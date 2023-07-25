package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VariableExpression(@NotNull Value value) implements Expression {

    public VariableExpression {
        Objects.requireNonNull(value);
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitVariableExpression(this);
    }
}
