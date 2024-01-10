package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private final @NotNull Context context;
    private final @NotNull Map<ReferenceExpression, Expr<?>> symbols = new HashMap<>();
    private final @NotNull Map<String, AtomicInteger> names = new HashMap<>();
    private final @NotNull Map<Snapshot, Expr<?>> optionality = new HashMap<>();

    private final @NotNull Deque<Expression> stack = new ArrayDeque<>();

    private final @NotNull List<BoolExpr> queue;

    private ConditionAnalyzer(@NotNull Context context, @NotNull List<BoolExpr> queue) {
        this.context = context;
        this.queue = queue;
    }

    public static boolean isSatisfiable(@NotNull DataFlowState state, @NotNull Set<Snapshot> targets) {
        if (targets.isEmpty()) {
            return true;
        }
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            getSolver(context, state, solver, targets);
            return switch (solver.check()) {
                case UNSATISFIABLE -> false;
                case UNKNOWN, SATISFIABLE -> true;
            };
        }
    }

    public static @NotNull Constraint getBooleanValue(@NotNull DataFlowState state, @NotNull Expression expression) {
        try (Context context = new Context()) {
            Set<Snapshot> targets = new HashSet<>();
            expression.iterate(expr -> {
                if (expr instanceof SnapshotExpression target) {
                    targets.add(target.getSnapshot());
                }
                return false;
            });
            Solver solver = context.mkSolver();
            ConditionAnalyzer conditionAnalyzer = getSolver(context, state, solver, targets);
            boolean isTrue = solver.check(conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(true)).accept(conditionAnalyzer))) != Status.UNSATISFIABLE;
            boolean isFalse = solver.check(conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(false)).accept(conditionAnalyzer))) != Status.UNSATISFIABLE;
            if (isTrue && isFalse) {
                return Constraint.ANY_VALUE;
            }
            if (isTrue) {
                return Constraint.ALWAYS_TRUE;
            }
            if (isFalse) {
                return Constraint.ALWAYS_FALSE;
            }
            return Constraint.NO_VALUE;
        }
    }

    public static @NotNull Optionality getOptionality(@NotNull DataFlowState state, @NotNull ReferenceExpression variable) {
        Constraint constraint = getBooleanValue(state, new UnaryExpression(UnaryOperator.PRESENT, variable));
        return switch (constraint) {
            case ANY_VALUE -> Optionality.UNKNOWN;
            case ALWAYS_TRUE -> Optionality.PRESENT;
            case ALWAYS_FALSE -> Optionality.MISSING;
            case NO_VALUE -> Optionality.NO_VALUE;
        };
    }

    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull Solver solver, @NotNull Set<Snapshot> targets) {
        List<BoolExpr> queue = new ArrayList<>();
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context, queue);
        Block functionBlock = state.getInstruction().getBlock();
        List<ArgumentGroup> argumentGroups = functionBlock.getArgumentGroups();
        DataFlowState firstPredecessor = Objects.requireNonNullElse(state.getFirstPredecessor(), state);
        for (ArgumentGroup argumentGroup : argumentGroups) {
            if (!(argumentGroup.isOptional()) || argumentGroup.arguments().size() <= 1) {
                continue;
            }
            List<Expr<BoolSort>> isOptional = new ArrayList<>();
            for (Argument argument : argumentGroup.arguments()) {
                SnapshotExpression snapshot = firstPredecessor.getSnapshot(new VariableExpression(argument));
                Objects.requireNonNull(snapshot);
                UnaryExpression present = new UnaryExpression(UnaryOperator.PRESENT, snapshot);
                BinaryExpression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, present, new LiteralExpression(true));
                isOptional.add(conditionAnalyzer.getAsBoolean(expression.accept(conditionAnalyzer)));
            }
            @SuppressWarnings("unchecked")
            Expr<BoolSort>[] array = isOptional.toArray(Expr[]::new);
            queue.add(context.mkAtMost(array, 1));
        }
        DataFlowState compactState = state.createCompactState(targets);
        for (Expression expression : compactState.getConditions()) {
            Expr<?> expr = expression.accept(conditionAnalyzer);
            if (expr.getSort().equals(context.getBoolSort())) {
                queue.add(conditionAnalyzer.getAsBoolean(expr));
            }
            if (expr.equals(context.mkFalse())) {
                break;
            }
        }
        if (queue.contains(context.mkFalse())) {
            solver.add(context.mkFalse());
            return conditionAnalyzer;
        }
        solver.add(queue.toArray(BoolExpr[]::new));
        return conditionAnalyzer;
    }

    private @NotNull Expr<?> getExpression(@NotNull Expression expression, @NotNull Expression child) {
        stack.add(expression);
        Expr<?> expr = child.accept(this);
        stack.removeLast();
        return expr;
    }

    @Override
    public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
        Expr<?> left = getExpression(expression, expression.getLeft());
        Expr<?> right = getExpression(expression, expression.getRight());
        return switch (expression.getOperator()) {
            case ADD -> {
                if (expression.getLeft().getType().isAssignable(RapidPrimitiveType.STRING) || expression.getRight().getType().isAssignable(RapidPrimitiveType.STRING)) {
                    yield context.mkConcat(getAsString(left), getAsString(right));
                } else {
                    yield context.mkAdd(getAsNumber(left), getAsNumber(right));
                }
            }
            case SUBTRACT -> context.mkSub(getAsNumber(left), getAsNumber(right));
            case MULTIPLY -> context.mkMul(getAsNumber(left), getAsNumber(right));
            case DIVIDE -> context.mkDiv(getAsNumber(left), getAsNumber(right));
            case INTEGER_DIVIDE -> context.mkDiv(getAsInt(left), getAsInt(right));
            case MODULO -> context.mkMod(getAsInt(left), getAsInt(right));
            case LESS_THAN -> context.mkLt(getAsNumber(left), getAsNumber(right));
            case LESS_THAN_OR_EQUAL -> context.mkLe(getAsNumber(left), getAsNumber(right));
            case EQUAL_TO -> {
                if (isEqualSort(left, right)) {
                    yield context.mkEq(left, right);
                } else {
                    yield context.mkBool(false);
                }
            }
            case NOT_EQUAL_TO -> getExpression(expression, new UnaryExpression(UnaryOperator.NOT, expression));
            case GREATER_THAN -> context.mkGt(getAsNumber(left), getAsNumber(right));
            case GREATER_THAN_OR_EQUAL -> context.mkGe(getAsNumber(left), getAsNumber(right));
            case AND -> context.mkAnd(getAsBoolean(left), getAsBoolean(right));
            case XOR -> context.mkXor(getAsBoolean(left), getAsBoolean(right));
            case OR -> context.mkOr(getAsBoolean(left), getAsBoolean(right));
        };
    }

    private boolean isEqualSort(Expr<?> left, Expr<?> right) {
        if (left.getSort().equals(context.getIntSort()) || left.getSort().equals(context.getRealSort())) {
            if (right.getSort().equals(context.getIntSort()) || right.getSort().equals(context.getRealSort())) {
                return true;
            }
        }
        return left.getSort().equals(right.getSort());
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<SeqSort<CharSort>> getAsString(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getStringSort())) {
            return (Expr<SeqSort<CharSort>>) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    private @NotNull BoolExpr getAsBoolean(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getBoolSort())) {
            return (BoolExpr) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<? extends ArithSort> getAsNumber(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getIntSort()) || sort.equals(context.getRealSort())) {
            return (Expr<? extends ArithSort>) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<IntSort> getAsInt(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getIntSort())) {
            return (Expr<IntSort>) expr;
        }
        if (sort.equals(context.getRealSort())) {
            return context.mkReal2Int((Expr<RealSort>) expr);
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @Override
    public Expr<?> visitUnaryExpression(@NotNull UnaryExpression expression) {
        Expression component = expression.getExpression();
        Expr<?> expr = getExpression(expression, component);
        return switch (expression.getOperator()) {
            case NOT -> context.mkNot(getAsBoolean(expr));
            case NEGATE -> context.mkMul(getAsNumber(expr), context.mkReal(-1));
            case PRESENT -> {
                if (component instanceof SnapshotExpression variable) {
                    Expr<?> handle = optionality.computeIfAbsent(variable.getSnapshot(), unused -> context.mkBoolConst("~" + variable.hashCode() + "*"));
                    yield context.mkEq(handle, context.mkTrue());
                }
                yield context.mkTrue();
            }
            case DIMENSION -> throw new IllegalArgumentException();
        };
    }

    private @NotNull RapidType getCorrectType(@NotNull Expression current) {
        if (!(current.getType().equals(RapidPrimitiveType.ANYTYPE))) {
            return current.getType();
        }
        Expression expression = stack.peekLast();
        if (expression == null) {
            return RapidPrimitiveType.BOOLEAN;
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return switch (unaryExpression.getOperator()) {
                case NOT, PRESENT -> RapidPrimitiveType.BOOLEAN;
                case NEGATE, DIMENSION -> RapidPrimitiveType.NUMBER;
            };
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            Expression alternative = binaryExpression.getLeft().equals(current) ? binaryExpression.getRight() : binaryExpression.getLeft();
            RapidType type = getCorrectType(alternative);
            return switch (binaryExpression.getOperator()) {
                case ADD -> {
                    if (RapidPrimitiveType.STRING.isAssignable(type)) {
                        yield RapidPrimitiveType.STRING;
                    }
                    yield RapidPrimitiveType.NUMBER;
                }
                case SUBTRACT, DIVIDE, MULTIPLY, MODULO, LESS_THAN, LESS_THAN_OR_EQUAL, INTEGER_DIVIDE, GREATER_THAN, GREATER_THAN_OR_EQUAL ->
                        RapidPrimitiveType.NUMBER;
                case EQUAL_TO, NOT_EQUAL_TO -> type;
                case AND, XOR, OR -> RapidPrimitiveType.BOOLEAN;
            };
        }
        if (expression instanceof AggregateExpression aggregateExpression) {
            RapidType type = getCorrectType(expression);
            if (type.getDimensions() > 0) {
                return type.createArrayType(type.getDimensions() - 1);
            }
            if (type.getRootStructure() instanceof RapidRecord record) {
                int index = aggregateExpression.getExpressions().indexOf(current);
                List<? extends RapidComponent> components = record.getComponents();
                if (index < 0 || index >= components.size()) {
                    return RapidPrimitiveType.BOOLEAN;
                }
                RapidType componentType = components.get(index).getType();
                return Objects.requireNonNullElse(componentType, RapidPrimitiveType.BOOLEAN);
            }
        }
        if (expression instanceof IndexExpression indexExpression) {
            if (current.equals(indexExpression.getIndex())) {
                return RapidPrimitiveType.NUMBER;
            }
            if (current.equals(indexExpression.getVariable())) {
                RapidType correctType = getCorrectType(indexExpression);
                return correctType.createArrayType(correctType.getDimensions() + 1);
            }
        }
        return RapidPrimitiveType.BOOLEAN;
    }

    @Override
    public Expr<?> visitSnapshotExpression(@NotNull SnapshotExpression expression) {
        if (symbols.containsKey(expression)) {
            return symbols.get(expression);
        }
        RapidType correctType = getCorrectType(expression);
        String name = "~" + expression.hashCode();
        if (names.containsKey(name)) {
            name += ":" + names.get(name).getAndIncrement();
        } else {
            names.put(name, new AtomicInteger());
        }
        Expr<?> expr;
        if (correctType.isAssignable(RapidPrimitiveType.STRING)) {
            expr = context.mkConst(name, context.mkStringSort());
        } else if (correctType.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            expr = context.mkConst(name, context.mkBoolSort());
        } else {
            expr = context.mkConst(name, context.mkRealSort());
        }
        symbols.put(expression, expr);
        String result = name;
        switch (expression.getSnapshot().getOptionality()) {
            case PRESENT -> {
                Expr<?> handle = optionality.computeIfAbsent(expression.getSnapshot(), unused -> context.mkBoolConst(result + "*"));
                queue.add(context.mkEq(handle, context.mkTrue()));
            }
            case MISSING -> {
                Expr<?> handle = optionality.computeIfAbsent(expression.getSnapshot(), unused -> context.mkBoolConst(result + "*"));
                queue.add(context.mkEq(handle, context.mkFalse()));
            }
            case NO_VALUE -> queue.add(context.mkFalse());
            case UNKNOWN -> {}
        }
        if (!(correctType.equals(expression.getType()))) {
            symbols.remove(expression);
        }
        return expr;
    }

    @Override
    public Expr<?> visitConstantExpression(@NotNull LiteralExpression expression) {
        Object object = expression.getValue();
        if (object instanceof String value) {
            return context.mkString(value);
        }
        if (object instanceof Boolean value) {
            return context.mkBool(value);
        }
        if (object instanceof Number value) {
            RapidType type = expression.getType();
            if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
                double v = value.doubleValue();
                if ((v % 1) == 0) {
                    return context.mkReal((long) v);
                }
                return context.mkFPToReal(context.mkFP(value.doubleValue(), context.mkFPSort(8, 24)));
            }
        }
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }

    @Override
    public Expr<?> visitExpression(@NotNull Expression expression) {
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }
}
