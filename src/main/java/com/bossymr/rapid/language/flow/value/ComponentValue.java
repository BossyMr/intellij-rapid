package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record ComponentValue(@NotNull RapidType type, @NotNull ReferenceValue variable, @NotNull String name) implements ReferenceValue {
    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitComponentVariableValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }
}
