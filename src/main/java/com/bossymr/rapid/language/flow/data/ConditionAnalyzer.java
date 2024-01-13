package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.expression.FunctionCallExpression.Entry;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private static final @NotNull Expression EMPTY_EXPRESSION = new SnapshotExpression(Snapshot.createSnapshot(RapidPrimitiveType.NUMBER));
    private final @NotNull Context context;

    private final @NotNull Map<ReferenceExpression, Expr<?>> symbols = new HashMap<>();
    private final @NotNull Map<Snapshot, Expr<?>> pointers = new HashMap<>();
    private final @NotNull Map<Integer, AtomicInteger> suffixes = new HashMap<>();

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
        Constraint constraint = getBooleanValue(state, FunctionCallExpression.present(variable));
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
        computeArgumentOptionality(state, conditionAnalyzer);
        List<DataFlowState> predecessorChain = state.createCompactState(targets).getPredecessorChain();
        for (int i = predecessorChain.size() - 1; i >= 0; i--) {
            DataFlowState predecessor = predecessorChain.get(i);
            for (Expression condition : predecessor.getConditions()) {
                Expr<?> expression = condition.accept(conditionAnalyzer);
                if (expression.getSort().equals(context.getBoolSort())) {
                    queue.add(conditionAnalyzer.getAsBoolean(expression));
                }
                if (queue.contains(context.mkFalse())) {
                    solver.add(context.mkFalse());
                    return conditionAnalyzer;
                }
            }
        }
        solver.add(queue.toArray(BoolExpr[]::new));
        System.out.println(solver);
        return conditionAnalyzer;
    }

    private static void computeArgumentOptionality(@NotNull DataFlowState state, @NotNull ConditionAnalyzer conditionAnalyzer) {
        Block functionBlock = state.getInstruction().getBlock();
        List<ArgumentGroup> argumentGroups = functionBlock.getArgumentGroups();
        for (ArgumentGroup argumentGroup : argumentGroups) {
            if (!(argumentGroup.isOptional()) || argumentGroup.arguments().size() <= 1) {
                continue;
            }
            List<Expr<BoolSort>> isOptional = new ArrayList<>();
            for (Argument argument : argumentGroup.arguments()) {
                SnapshotExpression snapshot = state.getSnapshot(new VariableExpression(argument));
                Objects.requireNonNull(snapshot);
                BinaryExpression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.present(snapshot), new LiteralExpression(true));
                isOptional.add(conditionAnalyzer.getAsBoolean(expression.accept(conditionAnalyzer)));
            }
            @SuppressWarnings("unchecked")
            Expr<BoolSort>[] array = isOptional.toArray(Expr[]::new);
            conditionAnalyzer.queue.add(conditionAnalyzer.context.mkAtMost(array, 1));
        }
    }

    private @NotNull Expr<?> getExpression(@Nullable Expression expression, @NotNull Expression child) {
        stack.add(Objects.requireNonNullElse(expression, EMPTY_EXPRESSION));
        Expr<?> expr = child.accept(this);
        stack.removeLast();
        return expr;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitFunctionCallExpression(@NotNull FunctionCallExpression expression) {
        List<Entry> arguments = expression.getArguments();
        return switch (expression.getName()) {
            case ":ConstantA" -> {
                Entry variable = arguments.get(0);
                Entry value = arguments.get(1);
                Sort arraySort = getSort(variable.variable().getType());
                Objects.requireNonNull(arraySort);
                yield context.mkConstArray(arraySort, getExpression(null, value.variable()));
            }
            case ":SelectA" -> {
                Expr array = getExpression(null, arguments.get(0).variable());
                Expr index = getExpression(null, arguments.get(1).variable());
                yield context.mkSelect(array, index);
            }
            case ":StoreA" -> {
                Expr array = getExpression(null, arguments.get(0).variable());
                Expr index = getExpression(null, arguments.get(1).variable());
                Expr value = getExpression(null, arguments.get(2).variable());
                yield context.mkStore(array, index, value);
            }
            case ":SelectS" -> {
                Expr<?> record = getExpression(null, arguments.get(0).variable());
                FuncDecl<?> field = getField(arguments.get(0).variable(), arguments.get(1).variable());
                yield context.mkApp(field, record);
            }
            case ":StoreS" -> {
                Expr<?> record = getExpression(null, arguments.get(0).variable());
                Expr<?> value = getExpression(null, arguments.get(2).variable());
                FuncDecl field = getField(arguments.get(0).variable(), arguments.get(1).variable());
                yield context.mkUpdateField(field, record, value);
            }
            default -> {
                Sort sort = Objects.requireNonNullElseGet(getSort(expression.getType()), context::getRealSort);
                Sort[] sorts = arguments.stream()
                                        .map(argument -> Objects.requireNonNullElseGet(getSort(argument.type()), context::getRealSort))
                                        .toList()
                                        .toArray(new Sort[0]);
                FuncDecl<?> funcDecl = context.mkFuncDecl(expression.getName(), sorts, sort);
                yield funcDecl.apply(getArguments(expression));
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private @NotNull FuncDecl<?> getField(@NotNull Expression variable, @NotNull Expression componentName) {
        Expr record = getExpression(null, variable);
        DatatypeSort sort = (DatatypeSort) record.getSort();
        FuncDecl.Parameter[] parameters = sort.getConstructors()[0].getParameters();
        FuncDecl.Parameter parameter = parameters[getComponentIndex(variable, ((String) ((LiteralExpression) componentName).getValue()))];
        return parameter.getFuncDecl();
    }

    private int getComponentIndex(@NotNull Expression variable, @NotNull String componentName) {
        RapidRecord record = (RapidRecord) variable.getType().getRootStructure();
        Objects.requireNonNull(record);
        List<? extends RapidComponent> components = record.getComponents();
        for (int i = 0; i < components.size(); i++) {
            if (componentName.equalsIgnoreCase(components.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private Expr<?> @NotNull [] getArguments(@NotNull FunctionCallExpression expression) {
        List<Entry> arguments = expression.getArguments();
        Expr<?>[] expressions = new Expr<?>[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            Entry entry = arguments.get(i);
            Expression argument = entry.variable();
            if (entry.type().equals(RapidPrimitiveType.ANYTYPE) && argument instanceof SnapshotExpression snapshot) {
                expressions[i] = createPointer(snapshot);
            } else {
                expressions[i] = getExpression(null, argument);
            }
        }
        return expressions;
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
        };
    }

    private @NotNull RapidType getCorrectType(@NotNull Expression current) {
        if (!(current.getType().equals(RapidPrimitiveType.ANYTYPE))) {
            return current.getType();
        }
        Expression expression = stack.peekLast();
        if (expression == null || expression.equals(EMPTY_EXPRESSION)) {
            return RapidPrimitiveType.ANYTYPE;
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return switch (unaryExpression.getOperator()) {
                case NOT -> RapidPrimitiveType.BOOLEAN;
                case NEGATE -> RapidPrimitiveType.NUMBER;
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
        return RapidPrimitiveType.ANYTYPE;
    }

    @SuppressWarnings("unchecked")
    private @Nullable Sort getSort(@NotNull RapidType type) {
        if (type.equals(RapidPrimitiveType.ANYTYPE)) {
            return null;
        }
        if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return context.mkStringSort();
        }
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return context.mkBoolSort();
        }
        if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
            return context.mkRealSort();
        }
        if (type.isArray()) {
            Sort sort = getSort(type.createArrayType(type.getDimensions() - 1));
            Objects.requireNonNull(sort);
            return context.mkArraySort(new Sort[]{context.getIntSort()}, sort);
        }
        if (type.isRecord()) {
            RapidRecord record = (RapidRecord) type.getRootStructure();
            Objects.requireNonNull(record);
            Sort[] sorts = record.getComponents().stream()
                                 .map(RapidVariable::getType)
                                 .filter(Objects::nonNull)
                                 .map(this::getSort)
                                 .peek(Objects::requireNonNull)
                                 .toList()
                                 .toArray(new Sort[0]);
            String qualifiedName = record.getQualifiedName();
            Objects.requireNonNull(qualifiedName);
            String[] componentNames = record.getComponents().stream()
                                            .map(RapidSymbol::getName)
                                            .filter(Objects::nonNull)
                                            .toList()
                                            .toArray(new String[0]);
            return context.mkDatatypeSort(qualifiedName, new Constructor[]{context.mkConstructor("create:" + qualifiedName, "is:" + qualifiedName, componentNames, sorts, null)});
        }
        return null;
    }

    private @NotNull Expr<?> createPointer(@NotNull SnapshotExpression expression) {
        return pointers.computeIfAbsent(expression.getSnapshot(), snapshot -> context.mkRealConst(getName(snapshot) + "*"));
    }

    @Override
    public Expr<?> visitSnapshotExpression(@NotNull SnapshotExpression expression) {
        if (symbols.containsKey(expression)) {
            return symbols.get(expression);
        }
        String name = getName(expression.getSnapshot());
        RapidType correctType = getCorrectType(expression);
        Sort sort = getSort(correctType);
        if (sort == null) {
            return createPointer(expression);
        }
        Expr<?> expr = context.mkConst(name, sort);
        symbols.put(expression, expr);
        switch (expression.getSnapshot().getOptionality()) {
            case PRESENT -> queue.add(getAsBoolean(FunctionCallExpression.present(expression).accept(this)));
            case MISSING -> {
                UnaryExpression unaryExpression = new UnaryExpression(UnaryOperator.NOT, FunctionCallExpression.present(expression));
                queue.add(getAsBoolean(unaryExpression.accept(this)));
            }
            case NO_VALUE -> queue.add(context.mkFalse());
            case UNKNOWN -> {}
        }
        if (!(correctType.equals(expression.getType()))) {
            symbols.remove(expression);
        }
        return expr;
    }

    private @NotNull String getName(@NotNull Snapshot snapshot) {
        int hashCode = snapshot.hashCode();
        if (suffixes.containsKey(hashCode)) {
            return hashCode + ":" + suffixes.get(hashCode).getAndIncrement();
        } else {
            suffixes.put(hashCode, new AtomicInteger());
            return String.valueOf(hashCode);
        }
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
                if ((long) v == v) {
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
