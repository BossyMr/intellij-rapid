package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record ComponentReference(@NotNull RapidType type, @NotNull ReferenceValue variable, @NotNull String component) implements ReferenceValue {
    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitComponentVariableValue(this);
    }
}
