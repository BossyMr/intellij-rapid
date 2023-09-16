package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VariableExpression implements ReferenceExpression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull Field field;

    public VariableExpression(@NotNull Field field) {
        this(null, field);
    }

    public VariableExpression(@Nullable RapidExpression expression, @NotNull Field field) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
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
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
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
