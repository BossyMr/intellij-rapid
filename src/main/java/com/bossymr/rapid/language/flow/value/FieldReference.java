package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FieldReference(@NotNull RapidType type, @Nullable String moduleName, @NotNull String name) implements ReferenceValue {
    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitFieldVariableValue(this);
    }
}
