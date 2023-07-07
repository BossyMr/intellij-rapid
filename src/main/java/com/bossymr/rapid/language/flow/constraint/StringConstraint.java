package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record StringConstraint(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Constraint {

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull InverseStringConstraint negate() {
        return new InverseStringConstraint(getOptionality(), sequences());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            StringConstraint copy = new StringConstraint(getOptionality().combine(constraint.getOptionality()), this.sequences());
            copy.sequences().removeAll(inverseStringCondition.sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = new StringConstraint(getOptionality().combine(constraint.getOptionality()), stringCondition.sequences());
            copy.sequences().retainAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            InverseStringConstraint copy = new InverseStringConstraint(inverseStringCondition.getOptionality(), inverseStringCondition.sequences());
            copy.sequences().removeAll(sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = new StringConstraint(stringCondition.getOptionality(), stringCondition.sequences());
            copy.sequences().addAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return sequences.isEmpty();
    }
}
