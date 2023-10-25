package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private final @NotNull Context context;

    private final @NotNull List<SnapshotExpression> snapshots = new ArrayList<>();
    private final @NotNull Map<ReferenceExpression, Symbol> symbols = new HashMap<>();

    private ConditionAnalyzer(@NotNull Context context) {
        this.context = context;
    }

    public static boolean isSatisfiable(@NotNull DataFlowState state) {
        if (state.getOptionality().containsValue(Optionality.NO_VALUE)) {
            return false;
        }
        try (Context context = new Context()) {
            ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context);
            Solver solver = getSolver(context, state, conditionAnalyzer);
            return switch (solver.check()) {
                case UNSATISFIABLE -> false;
                case UNKNOWN, SATISFIABLE -> true;
            };
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Solver getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull ConditionAnalyzer conditionAnalyzer) {
        Solver solver = context.mkSolver();
        List<Expression> expressions = getAllExpressions(state);
        for (int i = expressions.size() - 1; i >= 0; i--) {
            Expression expression = expressions.get(i);
            Expr<?> expr = expression.accept(conditionAnalyzer);
            if (expr.getSort() instanceof BoolSort) {
                solver.add((Expr<BoolSort>) expr);
            }
        }
        return solver;
    }

    private static @NotNull List<Expression> getAllExpressions(@NotNull DataFlowState state) {
        List<Expression> expressions = new ArrayList<>();
        getAllExpressions(expressions, state);
        return expressions;
    }

    private static void getAllExpressions(@NotNull List<Expression> expressions, @NotNull DataFlowState state) {
        for (int i = state.getExpressions().size() - 1; i >= 0; i--) {
            Expression expression = state.getExpressions().get(i);
            expressions.add(expression);
        }
        state.getPredecessor().ifPresent(predecessor -> getAllExpressions(expressions, predecessor));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
        Expr left = expression.getLeft().accept(this);
        Expr right = expression.getRight().accept(this);
        Expr<FPRMSort> sortExpr = context.mkConst("", context.mkFPRoundingModeSort());
        return switch (expression.getOperator()) {
            case ADD -> {
                if (expression.getLeft().getType().isAssignable(RapidPrimitiveType.STRING) || expression.getRight().getType().isAssignable(RapidPrimitiveType.STRING)) {
                    yield context.mkConcat(context.mkToRe(left), context.mkToRe(right));
                } else {
                    yield context.mkFPAdd(sortExpr, left, right);
                }
            }
            case SUBTRACT -> context.mkFPSub(sortExpr, left, right);
            case MULTIPLY -> context.mkFPMul(sortExpr, left, right);
            case DIVIDE -> context.mkFPDiv(sortExpr, left, right);
            case INTEGER_DIVIDE -> context.mkFPRoundToIntegral(sortExpr, context.mkFPDiv(sortExpr, left, right));
            case MODULO ->
                    context.mkMod(context.mkReal2Int(context.mkFPToReal(left)), context.mkReal2Int(context.mkFPToReal(right)));
            case LESS_THAN -> context.mkFPLt(left, right);
            case LESS_THAN_OR_EQUAL -> context.mkFPLEq(left, right);
            case EQUAL_TO -> {
                if (left.getSort().equals(right.getSort())) {
                    yield context.mkEq(left, right);
                } else {
                    yield context.mkBool(false);
                }
            }
            case NOT_EQUAL_TO -> new UnaryExpression(UnaryOperator.NOT, expression).accept(this);
            case GREATER_THAN -> context.mkFPGt(left, right);
            case GREATER_THAN_OR_EQUAL -> context.mkFPGEq(left, right);
            case AND -> context.mkAnd(left, right);
            case XOR -> context.mkXor(left, right);
            case OR -> context.mkOr(left, right);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitUnaryExpression(@NotNull UnaryExpression expression) {
        Expr expr = expression.getExpression().accept(this);
        return switch (expression.getOperator()) {
            case NOT -> context.mkNot(expr);
            case NEGATE -> context.mkFPNeg(expr);
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
            return context.mkConst(symbol, context.mkFPSort(0, 64));
        }
        RapidType type = expression.getType();
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
                return context.mkFP(value.doubleValue(), context.mkFPSort(8, 24));
            }
            if (type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                return context.mkFP(value.doubleValue(), context.mkFPSort(11, 53));
            }
        }
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }

    @Override
    public Expr<?> visitExpression(@NotNull Expression expression) {
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }
}
