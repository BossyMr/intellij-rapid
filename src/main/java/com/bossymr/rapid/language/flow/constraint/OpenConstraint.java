package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record OpenConstraint(@NotNull Optionality optionality) implements Constraint {

    @Override
    public @NotNull Set<Set<Condition>> toConditions(@NotNull ReferenceValue referenceValue) {
        return Set.of();
    }

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
