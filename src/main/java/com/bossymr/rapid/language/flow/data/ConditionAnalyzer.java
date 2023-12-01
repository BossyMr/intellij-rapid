package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private final @NotNull Context context;
    private final @NotNull Map<ReferenceExpression, Expr<?>> symbols = new HashMap<>();

    private final @NotNull Map<VariableSnapshot, Expr<?>> optionality = new HashMap<>();
    private final @NotNull EnumSort<?> optionalitySort;

    private ConditionAnalyzer(@NotNull Context context) {
        this.context = context;
        this.optionalitySort = context.mkEnumSort("optionality", "present", "missing");
    }

    public static boolean isSatisfiable(@NotNull DataFlowState state) {
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            getSolver(context, state, solver);
            return switch (solver.check()) {
                case UNSATISFIABLE -> false;
                case UNKNOWN, SATISFIABLE -> true;
            };
        }
    }

    public static @NotNull BooleanValue getBooleanValue(@NotNull DataFlowState state, @NotNull Expression expression) {
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            ConditionAnalyzer conditionAnalyzer = getSolver(context, state, solver);
            BoolExpr isPresentExpression = conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(true)).accept(conditionAnalyzer));
            boolean isTrue = solver.check(isPresentExpression) != Status.UNSATISFIABLE;
            BoolExpr isFalseExpression = conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(false)).accept(conditionAnalyzer));
            boolean isFalse = solver.check(isFalseExpression) != Status.UNSATISFIABLE;
            if (isTrue && isFalse) {
                return BooleanValue.ANY_VALUE;
            }
            if (isTrue) {
                return BooleanValue.ALWAYS_TRUE;
            }
            if (isFalse) {
                return BooleanValue.ALWAYS_FALSE;
            }
            return BooleanValue.NO_VALUE;
        }
    }

    public static @NotNull Optionality getOptionality(@NotNull DataFlowState state, @NotNull ReferenceExpression variable) {
        BooleanValue booleanValue = getBooleanValue(state, new UnaryExpression(UnaryOperator.PRESENT, variable));
        return switch (booleanValue) {
            case ANY_VALUE -> Optionality.UNKNOWN;
            case ALWAYS_TRUE -> Optionality.PRESENT;
            case ALWAYS_FALSE -> Optionality.MISSING;
            case NO_VALUE -> Optionality.NO_VALUE;
        };
    }

    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull Solver solver) {
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context);
        DataFlowBlock block = state.getBlock();
        if (block != null) {
            Block functionBlock = block.getInstruction().getBlock();
            List<ArgumentGroup> argumentGroups = functionBlock.getArgumentGroups();
            DataFlowState predecessor = Objects.requireNonNullElse(state.getFirstPredecessor(), state);
            for (ArgumentGroup argumentGroup : argumentGroups) {
                if (!(argumentGroup.isOptional()) || argumentGroup.arguments().size() <= 1) {
                    continue;
                }
                List<Expr<BoolSort>> isOptional = new ArrayList<>();
                for (Argument argument : argumentGroup.arguments()) {
                    SnapshotExpression snapshot = predecessor.getSnapshot(new VariableExpression(argument));
                    Objects.requireNonNull(snapshot);
                    UnaryExpression present = new UnaryExpression(UnaryOperator.PRESENT, snapshot);
                    BinaryExpression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, present, new LiteralExpression(true));
                    isOptional.add(conditionAnalyzer.getAsBoolean(expression.accept(conditionAnalyzer)));
                }
                @SuppressWarnings("unchecked")
                Expr<BoolSort>[] array = isOptional.toArray(Expr[]::new);
                solver.add(context.mkAtMost(array, 1));
            }
        }
        List<Expression> expressions = state.getAllExpressions();
        for (int i = expressions.size() - 1; i >= 0; i--) {
            Expression expression = expressions.get(i);
            Expr<?> expr = expression.accept(conditionAnalyzer);
            if (expr.getSort() instanceof BoolSort) {
                solver.add(conditionAnalyzer.getAsBoolean(expr));
            }
        }
        return conditionAnalyzer;
    }

    @Override
    public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
        Expr<?> left = expression.getLeft().accept(this);
        Expr<?> right = expression.getRight().accept(this);
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
                if (left.getSort().equals(right.getSort())) {
                    yield context.mkEq(left, right);
                } else {
                    yield context.mkBool(false);
                }
            }
            case NOT_EQUAL_TO -> new UnaryExpression(UnaryOperator.NOT, expression).accept(this);
            case GREATER_THAN -> context.mkGt(getAsNumber(left), getAsNumber(right));
            case GREATER_THAN_OR_EQUAL -> context.mkGe(getAsNumber(left), getAsNumber(right));
            case AND -> context.mkAnd(getAsBoolean(left), getAsBoolean(right));
            case XOR -> context.mkXor(getAsBoolean(left), getAsBoolean(right));
            case OR -> context.mkOr(getAsBoolean(left), getAsBoolean(right));
        };
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
        Expr<?> expr = component.accept(this);
        return switch (expression.getOperator()) {
            case NOT -> context.mkNot(getAsBoolean(expr));
            case NEGATE -> context.mkMul(getAsNumber(expr), context.mkReal(-1));
            case PRESENT -> {
                if (component instanceof VariableSnapshot variable) {
                    Expr<?> handle = optionality.computeIfAbsent(variable, unused -> context.mkConst("~" + variable.hashCode() + "*", optionalitySort));
                    yield context.mkEq(handle, optionalitySort.getConst(0));
                }
                yield context.mkBool(true);
            }
        };
    }

    @Override
    public Expr<?> visitSnapshotExpression(@NotNull SnapshotExpression expression) {
        if (symbols.containsKey(expression)) {
            return symbols.get(expression);
        }
        String name = "~" + expression.hashCode();
        RapidType type = expression.getType();
        if (type.isAssignable(RapidPrimitiveType.STRING)) {
            Expr<SeqSort<CharSort>> expr = context.mkConst(name, context.mkStringSort());
            symbols.put(expression, expr);
            return expr;
        }
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            Expr<BoolSort> expr = context.mkConst(name, context.mkBoolSort());
            symbols.put(expression, expr);
            return expr;
        }
        Expr<RealSort> expr = context.mkConst(name, context.mkRealSort());
        symbols.put(expression, expr);
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
