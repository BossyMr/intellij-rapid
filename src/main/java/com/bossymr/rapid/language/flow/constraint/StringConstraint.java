package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record StringConstraint(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Constraint {

    @Override
    public @NotNull StringConstraint copy(@NotNull Optionality optionality) {
        return new StringConstraint(optionality, sequences());
    }

    @Override
    public @NotNull InverseStringConstraint negate() {
        return new InverseStringConstraint(optionality(), sequences());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            StringConstraint copy = copy(optionality());
            copy.sequences().removeAll(inverseStringCondition.sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = stringCondition.copy(stringCondition.optionality());
            copy.sequences().retainAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            InverseStringConstraint copy = inverseStringCondition.copy(inverseStringCondition.optionality());
            copy.sequences().removeAll(sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = stringCondition.copy(stringCondition.optionality());
            copy.sequences().addAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }
}
