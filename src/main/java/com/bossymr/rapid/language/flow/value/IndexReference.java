package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record IndexReference(@NotNull RapidType type, @NotNull ReferenceValue variable, Value index) implements ReferenceValue {
    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitIndexVariableValue(this);
    }
}
