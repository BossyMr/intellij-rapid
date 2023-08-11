package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record StringConstraint(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Constraint {

    public static @NotNull StringConstraint anyOf(@NotNull String... sequences) {
        return new StringConstraint(Optionality.PRESENT, Set.of(sequences));
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull Constraint setOptionality(@NotNull Optionality optionality) {
        return new StringConstraint(optionality, new HashSet<>(sequences));
    }

    @Override
    public @NotNull Optional<String> getValue() {
        if (sequences().size() == 1) {
            return Optional.of(sequences().iterator().next());
        }
        return Optional.empty();
    }

    @Override
    public @NotNull InverseStringConstraint negate() {
        return new InverseStringConstraint(getOptionality(), new HashSet<>(sequences));
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.and(this);
        }
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            StringConstraint copy = new StringConstraint(getOptionality().and(constraint.getOptionality()), new HashSet<>(sequences));
            copy.sequences().removeAll(inverseStringCondition.sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = new StringConstraint(getOptionality().and(constraint.getOptionality()), new HashSet<>(stringCondition.sequences()));
            copy.sequences().retainAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException("Cannot create intersection of: " + this + " and " + constraint);
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.or(this);
        }
        if (constraint instanceof InverseStringConstraint inverseStringCondition) {
            InverseStringConstraint copy = new InverseStringConstraint(inverseStringCondition.getOptionality().or(constraint.getOptionality()), new HashSet<>(inverseStringCondition.sequences()));
            copy.sequences().removeAll(sequences());
            return copy;
        }
        if (constraint instanceof StringConstraint stringCondition) {
            StringConstraint copy = new StringConstraint(stringCondition.getOptionality().or(constraint.getOptionality()), new HashSet<>(stringCondition.sequences()));
            copy.sequences().addAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException("Cannot create union of: " + this + " and " + constraint);
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return sequences.isEmpty();
    }

    @Override
    public @NotNull String getPresentableText() {
        return "any of " + sequences();
    }
}
