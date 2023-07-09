package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AggregateExpression(@NotNull List<Value> values) implements Expression {
    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitAggregateExpression(this);
    }
}
