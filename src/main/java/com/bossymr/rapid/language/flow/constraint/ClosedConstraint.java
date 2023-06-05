package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record ClosedConstraint(@NotNull RapidType type) implements Constraint {

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return this;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return constraint;
    }

    @Override
    public @NotNull Constraint not() {
        return new OpenConstraint(type);
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        return false;
    }
}
