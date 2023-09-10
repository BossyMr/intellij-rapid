package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VariableExpression implements ReferenceExpression {

    private final @NotNull Field field;

    public VariableExpression(@NotNull Field field) {
        this.field = field;
    }

    public @NotNull Field getField() {
        return field;
    }

    @Override
    public @NotNull RapidType getType() {
        return field.getType();
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitVariableExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableExpression that = (VariableExpression) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }

    @Override
    public String toString() {
        return "VariableExpression{" +
                "variable=" + field +
                '}';
    }
}
