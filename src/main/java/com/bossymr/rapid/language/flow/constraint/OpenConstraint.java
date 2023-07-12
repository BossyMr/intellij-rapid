package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

public record OpenConstraint(@NotNull Optionality optionality) implements Constraint {

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull Constraint negate() {
        return new ClosedConstraint(getOptionality());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return constraint;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return this;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
