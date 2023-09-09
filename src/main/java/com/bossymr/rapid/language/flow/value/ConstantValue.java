package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@code ConstantValue} represents a constant value.
 */
public final class ConstantValue implements Value {

    private static final @NotNull ConstantValue TRUE = new ConstantValue(RapidPrimitiveType.BOOLEAN, true);
    private static final @NotNull ConstantValue FALSE = new ConstantValue(RapidPrimitiveType.BOOLEAN, false);
    private static final @NotNull ConstantValue ZERO = new ConstantValue(RapidPrimitiveType.NUMBER, 0);
    private static final @NotNull ConstantValue ONE = new ConstantValue(RapidPrimitiveType.NUMBER, 1);
    private static final @NotNull ConstantValue EMPTY_STRING = new ConstantValue(RapidPrimitiveType.BOOLEAN, "");

    private final @NotNull RapidType type;
    private final @NotNull Object value;

    private ConstantValue(@NotNull RapidType type, @NotNull Object value) {
        this.type = type;
        this.value = value;
    }

    public static @NotNull Value of(@NotNull RapidType type, double object) {
        return of(type, (Object) object);
    }

    public static @NotNull Value of(@NotNull RapidType type, boolean object) {
        return of(type, (Object) object);
    }


    public static @NotNull Value of(@NotNull RapidType type, @NotNull Object object) {
        if (object instanceof Boolean value) {
            if (!(type.isAssignable(RapidPrimitiveType.BOOLEAN))) {
                return ErrorValue.getInstance();
            }
            return of(value);
        }
        if (object instanceof Number value) {
            if (!(type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE))) {
                return ErrorValue.getInstance();
            }
            return of(value.doubleValue());
        }
        if (object instanceof String value) {
            if (!(type.isAssignable(RapidPrimitiveType.STRING))) {
                return ErrorValue.getInstance();
            }
            return of(value);
        }
        return ErrorValue.getInstance();
    }

    public static @NotNull ConstantValue of(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static @NotNull ConstantValue of(double value) {
        if (value == 0) {
            return ZERO;
        }
        if (value == 1) {
            return ONE;
        }
        return new ConstantValue(RapidPrimitiveType.NUMBER, value);
    }

    public static @NotNull ConstantValue of(@NotNull String value) {
        if (value.isEmpty()) {
            return EMPTY_STRING;
        }
        return new ConstantValue(RapidPrimitiveType.STRING, value);
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitConstantValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    public @NotNull Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConstantValue that = (ConstantValue) object;
        return Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "ConstantValue{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
