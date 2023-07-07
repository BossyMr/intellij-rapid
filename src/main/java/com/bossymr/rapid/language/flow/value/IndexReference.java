package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record IndexReference(@NotNull RapidType type, @NotNull ReferenceValue variable, Value index) implements ReferenceValue {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitIndexVariableValue(this);
    }
}
