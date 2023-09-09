package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FieldValue(@NotNull RapidType type, @Nullable String moduleName, @NotNull String name) implements ReferenceValue {
    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitFieldVariableValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }
}
