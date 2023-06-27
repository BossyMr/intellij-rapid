package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

public record TopConstraint(@NotNull Optionality optionality) implements Constraint {

    @Override
    public @NotNull Constraint copy(@NotNull Optionality optionality) {
        return new TopConstraint(optionality);
    }

    @Override
    public @NotNull Constraint negate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return constraint;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return this;
    }
}
