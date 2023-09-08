package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record IndexValue(@NotNull ReferenceValue variable, @NotNull Value index) implements ReferenceValue {
    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitIndexValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        RapidType type = variable.getType();
        return type.createArrayType(type.getDimensions() - 1);
    }
}
