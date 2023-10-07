package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ConstantExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull Object value;

    public ConstantExpression(@NotNull RapidType type, @NotNull Object value) {
        this(null, type, value);
    }

    public ConstantExpression(@Nullable RapidExpression expression, @NotNull RapidType type, @NotNull Object value) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.type = type;
        this.value = value;
    }

    public @NotNull Object getValue() {
        return value;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitConstantExpression(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantExpression that = (ConstantExpression) o;
        return Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return value instanceof String ? "\"" + value + "\"" : String.valueOf(value);
    }
}
