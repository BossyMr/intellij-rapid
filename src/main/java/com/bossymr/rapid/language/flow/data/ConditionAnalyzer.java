package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint.BooleanValue;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionAnalyzer extends ControlFlowVisitor<BooleanValue> {

    private final @NotNull DataFlowState state;

    public ConditionAnalyzer(@NotNull DataFlowState state) {
        this.state = state;
    }

    @Override
    public @NotNull BooleanValue visitCondition(@NotNull Condition condition) {
        try (Context context = new Context()) {
            Map<ReferenceValue, Symbol> symbols = new HashMap<>();
            List<ReferenceSnapshot> snapshots = new ArrayList<>();
            for (Condition relation : state.getConditions()) {
                ReferenceValue variable = relation.getVariable();
                symbols.computeIfAbsent(variable, (unused) -> createSymbol(context, variable, snapshots));
                Symbol symbol = symbols.get(variable);
                Expression expression = relation.getExpression();

            }
        }
        return super.visitCondition(condition);
    }

    private @NotNull Expr<?> createExpression(@NotNull Context context, @NotNull Expression expression, @NotNull Map<ReferenceValue, Symbol> symbols, @NotNull List<ReferenceSnapshot> snapshots) {
        return expression.accept(new ControlFlowVisitor<>() {
            @Override
            public Expr<?> visitValueExpression(@NotNull ValueExpression expression) {
                return super.visitValueExpression(expression);
            }

            @Override
            public Expr<?> visitAggregateExpression(@NotNull AggregateExpression expression) {
                return super.visitAggregateExpression(expression);
            }

            @Override
            public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
                return super.visitBinaryExpression(expression);
            }

            @Override
            public Expr<?> visitUnaryExpression(@NotNull UnaryExpression expression) {
                return super.visitUnaryExpression(expression);
            }
        });
    }

    private @NotNull Symbol createSymbol(@NotNull Context context, @NotNull ReferenceValue variable, @NotNull List<ReferenceSnapshot> snapshots) {
        if (!(variable instanceof ReferenceSnapshot snapshot)) {
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
        if (!(value.getType().isAssignable(RapidType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for: " + value);
        }
        return null;
    }

    @Override
    public @NotNull BooleanValue visitErrorValue(@NotNull ErrorValue value) {
        if (!(value.getType().isAssignable(RapidType.BOOLEAN))) {
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
