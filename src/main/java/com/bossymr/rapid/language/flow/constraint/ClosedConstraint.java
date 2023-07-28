package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClosedConstraint(@NotNull Optionality optionality) implements Constraint {

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull Constraint setOptionality(@NotNull Optionality optionality) {
        return new ClosedConstraint(optionality);
    }

    @Override
    public @NotNull Optional<?> getValue() {
        return Optional.empty();
    }

    @Override
    public @NotNull Constraint negate() {
        return new OpenConstraint(getOptionality());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return setOptionality(optionality.and(constraint.getOptionality()));
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return constraint.setOptionality(optionality.or(constraint.getOptionality()));
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public @NotNull String getPresentableText() {
        return "none";
    }
}
