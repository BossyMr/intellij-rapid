package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record InverseStringConstraint(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Constraint {

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull StringConstraint negate() {
        return new StringConstraint(getOptionality(), sequences());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return constraint.and(this);
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return constraint.or(this);
    }

    @Override
    public boolean isFull() {
        return sequences.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
