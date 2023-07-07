package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

public record VariableExpression(@NotNull Value value) implements Expression {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitVariableExpression(this);
    }
}
