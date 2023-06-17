package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code MissingConstraint} represents a constraint might be a missing value.
 *
 * @param type the type of value.
 * @param constraint the constraint of the value if it is present.
 */
public record MissingConstraint(@NotNull RapidType type, @NotNull Constraint constraint) implements Constraint {

    public MissingConstraint {
        if (constraint instanceof MissingConstraint) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof MissingConstraint missingConstraint) {
            return new MissingConstraint(getType(), constraint().or(missingConstraint.constraint()));
        }
        return new MissingConstraint(getType(), constraint().or(constraint));
    }

    @Override
    public @NotNull Constraint not() {
        return constraint().not();
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        return constraint().contains(constraint);
    }
}
