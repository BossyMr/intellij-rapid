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
