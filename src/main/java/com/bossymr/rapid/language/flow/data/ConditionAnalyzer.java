package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionAnalyzer extends ControlFlowVisitor<BooleanValue> {

    private final @NotNull DataFlowState state;

    public ConditionAnalyzer(@NotNull DataFlowState state) {
        this.state = state;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public @NotNull BooleanValue visitExpression(@NotNull Condition condition) {
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            Map<ReferenceValue, Symbol> symbols = new HashMap<>();
            List<SnapshotExpression> snapshots = new ArrayList<>();
            // TODO: 2023-09-10 The order must be oldest-newest
            for (Condition relation : state.getAllExpressions()) {
                Expr expr = createCondition(context, relation, symbols, snapshots);
                if(expr == null) {
                    continue;
                }
                solver.add(expr);
            }
            Expr expr = createCondition(context, condition, symbols, snapshots);
            if(expr == null) {
                return BooleanValue.ANY_VALUE;
            }
            solver.add(expr);
            Expr<?> symbol = getSymbol(context, condition.getVariable(), symbols, snapshots);
            Status isTrue = solver.check(context.mkEq(symbol, context.mkBool(true)));
            Status isFalse = solver.check(context.mkEq(symbol, context.mkBool(false)));
            if (isTrue == Status.UNKNOWN || isFalse == Status.UNKNOWN) {
                return BooleanValue.ANY_VALUE;
            }
            boolean mightBeTrue = isTrue == Status.SATISFIABLE;
            boolean mightBeFalse = isFalse != Status.SATISFIABLE;
            if (mightBeTrue && mightBeFalse) {
                return BooleanValue.ANY_VALUE;
            }
            if (mightBeTrue) {
                return BooleanValue.ALWAYS_TRUE;
            }
            if (mightBeFalse) {
                return BooleanValue.ALWAYS_FALSE;
            }
            return BooleanValue.NO_VALUE;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private @Nullable Expr createCondition(@NotNull Context context, @NotNull Condition condition, @NotNull Map<ReferenceValue, Symbol> symbols, @NotNull List<SnapshotExpression> snapshots) {
        ReferenceValue variable = condition.getVariable();
        Expression expression = condition.getExpression();
        Expr expr = createExpression(context, expression, symbols, snapshots);
        if(expr == null) {
            return null;
        }
        Expr realConst = getSymbol(context, variable, symbols, snapshots);
        return switch (condition.getConditionType()) {
            case EQUALITY -> context.mkEq(realConst, expr);
            case INEQUALITY -> context.mkNot(context.mkEq(realConst, expr));
            case LESS_THAN -> context.mkLt(realConst, expr);
            case LESS_THAN_OR_EQUAL -> context.mkLe(realConst, expr);
            case GREATER_THAN -> context.mkGt(realConst, expr);
            case GREATER_THAN_OR_EQUAL -> context.mkGe(realConst, expr);
        };
    }

    private @Nullable Expr<?> createExpression(@NotNull Context context, @NotNull Expression expression, @NotNull Map<ReferenceValue, Symbol> symbols, @NotNull List<SnapshotExpression> snapshots) {
        return expression.accept(new ControlFlowVisitor<>() {
            @Override
            public Expr<?> visitValueExpression(@NotNull ValueExpression expression) {
                return getSymbol(context, expression.value(), symbols, snapshots);
            }

            @Override
            public Expr<?> visitAggregateExpression(@NotNull AggregateExpression expression) {
                throw new IllegalArgumentException("Unexpected expression: " + expression);
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
                Expr left = getSymbol(context, expression.left(), symbols, snapshots);
                Expr right = getSymbol(context, expression.right(), symbols, snapshots);
                if(left == null || right == null) {
                    return null;
                }
                return switch (expression.operator()) {
                    case ADD -> context.mkAdd(left, right);
                    case SUBTRACT -> context.mkSub(left, right);
                    case MULTIPLY -> context.mkMul(left, right);
                    case DIVIDE -> context.mkDiv(left, right);
                    case INTEGER_DIVIDE ->
                            context.mkFPRoundToIntegral(context.mkFPRoundTowardNegative(), context.mkDiv(left, right));
                    case MODULO -> context.mkMod(left, right);
                    case LESS_THAN -> context.mkLt(left, right);
                    case LESS_THAN_OR_EQUAL -> context.mkLe(left, right);
                    case EQUAL_TO -> context.mkEq(left, right);
                    case NOT_EQUAL_TO -> context.mkNot(context.mkEq(left, right));
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
                Expr expr = getSymbol(context, expression.value(), symbols, snapshots);
                if(expr == null) {
                    return null;
                }
                return switch (expression.operator()) {
                    case NOT -> context.mkNot(expr);
                    case NEGATE -> context.mkFPNeg(expr);
                };
            }
        });
    }

    private @Nullable Expr<?> getSymbol(@NotNull Context context, @NotNull Value value, @NotNull Map<ReferenceValue, Symbol> symbols, @NotNull List<SnapshotExpression> snapshots) {
        if (value instanceof ReferenceValue variable) {
            Symbol symbol = symbols.containsKey(variable) ? symbols.get(variable) : createSymbol(context, variable, snapshots);
            symbols.put(variable, symbol);
            RapidType type = variable.getType();
            if (type.isAssignable(RapidPrimitiveType.STRING)) {
                return context.mkConst(symbol, context.mkStringSort());
            }
            if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
                return context.mkConst(symbol, context.mkBoolSort());
            }
            if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
                return context.mkConst(symbol, context.mkFPSort(8, 24));
            }
            if (type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                return context.mkConst(symbol, context.mkFPSort(11, 53));
            }
            throw new IllegalArgumentException();
        }
        if (value instanceof ErrorValue) {
            return null;
        }
        if (value instanceof ConstantValue constantValue) {
            Object object = constantValue.getValue();
            if (object instanceof String) {
                return context.mkString((String) object);
            }
            if (object instanceof Boolean) {
                return context.mkBool((Boolean) object);
            }
            RapidType type = value.getType();
            if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
                return context.mkFP(((Number) object).doubleValue(), context.mkFPSort(8, 24));
            }
            if (type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                return context.mkFP(((Number) object).doubleValue(), context.mkFPSort(11, 53));
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }

    private @NotNull Symbol createSymbol(@NotNull Context context, @NotNull ReferenceValue variable, @NotNull List<SnapshotExpression> snapshots) {
        if (!(variable instanceof SnapshotExpression snapshot)) {
            throw new IllegalArgumentException();
        }
        if (snapshots.contains(snapshot)) {
            throw new IllegalStateException();
        }
        snapshots.add(snapshot);
        return context.mkSymbol(snapshots.indexOf(snapshot));
    }

    @Override
    public @NotNull BooleanValue visitReferenceValue(@NotNull ReferenceValue value) {
        if (!(value.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for: " + value);
        }
        return null;
    }

    @Override
    public @NotNull BooleanValue visitErrorValue(@NotNull ErrorValue value) {
        if (!(value.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for: " + value);
        }
        return BooleanValue.ANY_VALUE;
    }

    @Override
    public @NotNull BooleanValue visitConstantValue(@NotNull ConstantValue constantValue) {
        Object object = constantValue.getValue();
        if (!(object instanceof Boolean value)) {
            throw new IllegalArgumentException("Cannot calculate constraint for: " + constantValue);
        }
        return BooleanValue.of(value);
    }

    @Override
    public @NotNull BooleanValue visitValueExpression(@NotNull ValueExpression expression) {
        Value value = expression.value();
        return value.accept(this);
    }
}
