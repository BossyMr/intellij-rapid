package com.bossymr.rapid.language.flow.expression;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LiteralExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;
    private final @NotNull RapidType type;
    private final @NotNull Object value;

    public LiteralExpression(@NotNull Object value) {
        this(null, value);
    }

    public LiteralExpression(@Nullable RapidExpression expression, @NotNull Object value) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.type = Objects.requireNonNull(getType(value));
        this.value = value;
    }

    private static @Nullable RapidType getType(@NotNull Object value) {
        if (value instanceof String) {
            return RapidPrimitiveType.STRING;
        }
        if (value instanceof Double || value instanceof Long) {
            return RapidPrimitiveType.DOUBLE;
        }
        if (value instanceof Byte || value instanceof Character || value instanceof Float || value instanceof Integer) {
            return RapidPrimitiveType.NUMBER;
        }
        if (value instanceof Boolean) {
            return RapidPrimitiveType.BOOLEAN;
        }
        return null;
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
        LiteralExpression that = (LiteralExpression) o;
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
