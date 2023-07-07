package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record ComponentReference(@NotNull RapidType type, @NotNull ReferenceValue variable, @NotNull String component) implements ReferenceValue {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitComponentVariableValue(this);
    }
}
