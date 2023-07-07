package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record ErrorValue() implements Value {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitErrorValue(this);
    }

    @Override
    public @NotNull RapidType type() {
        return RapidType.BOOLEAN;
    }
}
