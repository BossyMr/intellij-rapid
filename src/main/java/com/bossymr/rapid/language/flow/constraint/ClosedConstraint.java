package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

public record ClosedConstraint(@NotNull Optionality optionality) implements Constraint {

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull Constraint copy(@NotNull Optionality optionality) {
        return new ClosedConstraint(optionality);
    }

    @Override
    public @NotNull Constraint negate() {
        return new OpenConstraint(getOptionality());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return this;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return constraint;
    }
}
