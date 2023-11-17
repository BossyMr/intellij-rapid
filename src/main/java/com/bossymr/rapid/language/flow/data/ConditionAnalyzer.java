package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private final @NotNull Context context;
    private final @NotNull Consumer<Expr<?>> consumer;
    private final @NotNull DataFlowState state;

    private final @NotNull List<SnapshotExpression> snapshots = new ArrayList<>();
    private final @NotNull Map<ReferenceExpression, Symbol> symbols = new HashMap<>();

    private ConditionAnalyzer(@NotNull Context context, @NotNull BiConsumer<ConditionAnalyzer, Expr<?>> consumer, @NotNull DataFlowState state) {
        this.context = context;
        this.consumer = expr -> consumer.accept(this, expr);
        this.state = state;
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

    @SuppressWarnings("unchecked")
    public static boolean isConstant(@NotNull DataFlowState state, @NotNull Expression expression) {
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            VariableSnapshot snapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
            BinaryExpression equality = new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, expression);
            ConditionAnalyzer conditionAnalyzer = getSolver(context, state, solver);
            solver.add((Expr<BoolSort>) equality.accept(conditionAnalyzer));
            Expr<?> variable = snapshot.accept(conditionAnalyzer);
            if (solver.check() != Status.SATISFIABLE) {
                return false;
            }
            Expr<?> firstValue = solver.getModel().eval(variable, true);
            if (firstValue == null) {
                return false;
            }
            if (solver.check(context.mkNot(context.mkEq(variable, firstValue))) != Status.SATISFIABLE) {
                return true;
            }
            Expr<?> secondValue = solver.getModel().eval(variable, true);
            return solver.check(context.mkEq(firstValue, secondValue)) == Status.SATISFIABLE;
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull Solver solver) {
        return getSolver(context, state, (analyzer, expr) -> solver.add((Expr<BoolSort>) expr));
    }

    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull BiConsumer<ConditionAnalyzer, Expr<?>> consumer) {
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context, consumer, state);
        List<Expression> expressions = state.getAllExpressions();
        for (int i = expressions.size() - 1; i >= 0; i--) {
            Expression expression = expressions.get(i);
            Expr<?> expr = expression.accept(conditionAnalyzer);
            if (expr.getSort() instanceof BoolSort) {
                consumer.accept(conditionAnalyzer, expr);
            }
        }
        return conditionAnalyzer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
        Expr left = expression.getLeft().accept(this);
        Expr right = expression.getRight().accept(this);
        return switch (expression.getOperator()) {
            case ADD -> {
                if (expression.getLeft().getType().isAssignable(RapidPrimitiveType.STRING) || expression.getRight().getType().isAssignable(RapidPrimitiveType.STRING)) {
                    yield context.mkConcat(left, right);
                } else {
                    yield context.mkAdd(left, right);
                }
            }
            case SUBTRACT -> context.mkSub(left, right);
            case MULTIPLY -> context.mkMul(left, right);
            case DIVIDE -> context.mkDiv(left, right);
            case INTEGER_DIVIDE ->
                    context.mkInt2Real(context.mkDiv(context.mkReal2Int(left), context.mkReal2Int(right)));
            case MODULO ->
                    context.mkMod(context.mkReal2Int(context.mkFPToReal(left)), context.mkReal2Int(context.mkFPToReal(right)));
            case LESS_THAN -> context.mkLt(left, right);
            case LESS_THAN_OR_EQUAL -> context.mkLe(left, right);
            case EQUAL_TO -> {
                if (left.getSort().equals(right.getSort())) {
                    yield context.mkEq(left, right);
                } else {
                    yield context.mkBool(false);
                }
            }
            case NOT_EQUAL_TO -> new UnaryExpression(UnaryOperator.NOT, expression).accept(this);
            case GREATER_THAN -> context.mkGt(left, right);
            case GREATER_THAN_OR_EQUAL -> context.mkGe(left, right);
            case AND -> context.mkAnd(left, right);
            case XOR -> context.mkXor(left, right);
            case OR -> context.mkOr(left, right);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitUnaryExpression(@NotNull UnaryExpression expression) {
        Expression component = expression.getExpression();
        Expr expr = component.accept(this);
        return switch (expression.getOperator()) {
            case NOT -> context.mkNot(expr);
            case NEGATE -> context.mkMul(expr, context.mkReal(-1));
            case PRESENT -> {
                if (expression instanceof ReferenceExpression variable) {
                    Optionality optionality = state.getOptionality(variable);
                    yield switch (optionality) {
                        case PRESENT -> context.mkBool(true);
                        case MISSING -> context.mkBool(false);
                        case UNKNOWN -> {
                            snapshots.add(null);
                            yield context.mkConst(context.mkSymbol(snapshots.size() - 1), context.mkBoolSort());
                        }
                        case NO_VALUE -> {
                            snapshots.add(null);
                            Expr<BoolSort> result = context.mkConst(context.mkSymbol(snapshots.size() - 1), context.mkBoolSort());
                            consumer.accept(context.mkEq(result, context.mkBool(true)));
                            consumer.accept(context.mkEq(result, context.mkBool(false)));
                            yield result;
                        }
                    };
                }
                yield context.mkBool(true);
            }
        };
    }

    @Override
    public Expr<?> visitSnapshotExpression(@NotNull SnapshotExpression expression) {
        if (!(symbols.containsKey(expression))) {
            snapshots.add(expression);
            symbols.put(expression, context.mkSymbol(snapshots.indexOf(expression)));
        }
        Symbol symbol = symbols.get(expression);
        if (expression instanceof PathCounter) {
            return context.mkConst(symbol, context.mkRealSort());
        }
        RapidType type = expression.getType();
        if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return context.mkConst(symbol, context.mkStringSort());
        }
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return context.mkConst(symbol, context.mkBoolSort());
        }
        if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
            return context.mkConst(symbol, context.mkRealSort());
        }
        if (type.isAssignable(RapidPrimitiveType.DOUBLE)) {
            return context.mkConst(symbol, context.mkRealSort());
        }
        return context.mkConst(symbol, context.mkRealSort());
    }

    @Override
    public Expr<?> visitConstantExpression(@NotNull ConstantExpression expression) {
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
                return context.mkFPToReal(context.mkFP(value.doubleValue(), context.mkFPSort(8, 24)));
            }
            if (type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                return context.mkFPToReal(context.mkFP(value.doubleValue(), context.mkFPSort(11, 53)));
            }
        }
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }

    @Override
    public Expr<?> visitExpression(@NotNull Expression expression) {
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }
}
