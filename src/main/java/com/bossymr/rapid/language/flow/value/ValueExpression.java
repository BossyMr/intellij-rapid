package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ValueExpression(@NotNull Value value) implements Expression {

    public ValueExpression {
        Objects.requireNonNull(value);
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitValueExpression(this);
    }
}
