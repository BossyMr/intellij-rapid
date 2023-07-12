package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code ConstantValue} represents a constant value.
 *
 * @param value the value.
 */
public record ConstantValue(@NotNull RapidType type, @NotNull Object value) implements Value {
    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitConstantValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }
}
