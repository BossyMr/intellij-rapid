package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code ConstantValue} represents a constant value.
 *
 * @param type the type.
 * @param value the value.
 */
public record ConstantValue(@NotNull RapidType type, @NotNull Object value) implements Value {

    public ConstantValue {
        assert !(type.isAssignable(RapidType.DOUBLE) || type.isAssignable(RapidType.NUMBER)) || value instanceof Number : "Cannot create constant value of type: " + type + " with value: " + value;
        assert !(type.isAssignable(RapidType.STRING)) || value instanceof String : "Cannot create constant value of type: " + type + " with value: " + value;
        assert !(type.isAssignable(RapidType.BOOLEAN)) || value instanceof Boolean : "Cannot create constant value of type: " + type + " with value: " + value;
    }

    public static @NotNull ConstantValue of(boolean value) {
        return new ConstantValue(RapidType.BOOLEAN, value);
    }

    public static @NotNull ConstantValue of(double value) {
        return new ConstantValue(RapidType.NUMBER, value);
    }

    public static @NotNull ConstantValue of(@NotNull String value) {
        return new ConstantValue(RapidType.STRING, value);
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitConstantValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }
}
