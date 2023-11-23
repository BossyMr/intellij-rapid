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

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private final @NotNull Context context;
    private final @NotNull Solver solver;
    private final @NotNull DataFlowState state;

    private final @NotNull List<SnapshotExpression> snapshots = new ArrayList<>();
    private final @NotNull Map<ReferenceExpression, Expr<?>> symbols = new HashMap<>();

    private ConditionAnalyzer(@NotNull Context context, @NotNull Solver solver, @NotNull DataFlowState state) {
        this.context = context;
        this.solver = solver;
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

    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull Solver solver) {
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context, solver, state);
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
                            solver.add(context.mkEq(result, context.mkBool(true)));
                            solver.add(context.mkEq(result, context.mkBool(false)));
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
        if (symbols.containsKey(expression)) {
            return symbols.get(expression);
        }
        snapshots.add(expression);
        String name = "~" + expression.hashCode();
        if (expression instanceof PathCounter pathCounter) {
            Expr<IntSort> expr = context.mkConst(name, context.mkIntSort());
            symbols.put(expression, expr);
            solver.add(context.mkGe(expr, context.mkInt(0)));
            if (pathCounter.reset().contains(pathCounter.increment())) {
                solver.add(context.mkLe(expr, context.mkInt(1)));
            }
            StringSymbol symbol = context.mkSymbol("n");
            Sort[] sorts = {context.getIntSort()};
            Symbol[] symbols = {symbol};
            IntExpr n = context.mkIntConst(symbol);
            VariableSnapshot snapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
            snapshots.add(snapshot);
            this.symbols.put(snapshot, n);
            List<BoolExpr> exits = new ArrayList<>();
            for (Expression value : pathCounter.increment().guards().values()) {
                if (value == null) {
                    continue;
                }
                value = value.replace(e -> {
                    if (!(e instanceof ReferenceExpression variable)) {
                        return e;
                    }
                    Expression definition = state.getExpression(variable);
                    if (definition.getComponents().contains(pathCounter)) {
                        return definition.replace(component -> component == pathCounter ? snapshot : component);
                    }
                    SnapshotExpression result = state.getSnapshot(variable);
                    return result != null ? result : e;
                });
                BoolExpr result = getAsBoolean(value.accept(this));
                exits.add(result);
            }
            if (!exits.isEmpty()) {
                BoolExpr boundInside = context.mkAnd(context.mkGe(n, context.mkInt(0)), context.mkLt(n, expr));
                BoolExpr boundOutside = context.mkGt(n, expr);
                BoolExpr impliesInside = context.mkImplies(boundInside, exits.size() > 1 ? context.mkAnd(exits.stream()
                                                                                                              .map(exit -> context.mkEq(exit, context.mkTrue())).toArray(BoolExpr[]::new)) : context.mkEq(exits.get(0), context.mkTrue()));
                BoolExpr impliesOutside = context.mkImplies(boundOutside, exits.size() > 1 ? context.mkAnd(exits.stream()
                                                                                                                .map(exit -> context.mkEq(exit, context.mkFalse())).toArray(BoolExpr[]::new)) : context.mkEq(exits.get(0), context.mkFalse()));
                BoolExpr implies = context.mkAnd(impliesInside, impliesOutside);
                Quantifier quantifier = context.mkForall(sorts, symbols, implies, 1, null, null, null, null);
                solver.add(quantifier);
            }
            return expr;
        }
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
