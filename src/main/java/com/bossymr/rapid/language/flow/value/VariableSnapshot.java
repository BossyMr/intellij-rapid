package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record VariableSnapshot(@NotNull ReferenceValue variable, int index) implements ReferenceValue {

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        variable.accept(visitor);
    }

    @Override
    public @NotNull RapidType type() {
        return variable.type();
    }
}
