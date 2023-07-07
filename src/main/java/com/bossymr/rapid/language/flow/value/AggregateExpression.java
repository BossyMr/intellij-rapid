package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AggregateExpression(@NotNull List<Value> values) implements Expression {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitAggregateExpression(this);
    }
}
