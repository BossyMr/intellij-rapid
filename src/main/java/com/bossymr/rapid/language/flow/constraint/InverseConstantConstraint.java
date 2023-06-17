package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * An {@code InverseConstantConstraint} is a constraint where a value is guaranteed to not be one of the specified
 * values.
 *
 * @param type the type of value.
 * @param values the values which this value will not be equal to.
 */
public record InverseConstantConstraint(@NotNull RapidType type, @NotNull Set<Object> values) implements Constraint {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof InverseConstantConstraint inverseConstantConstraint) {
            InverseConstantConstraint copy = new InverseConstantConstraint(type(), values());
            copy.values().removeIf(value -> !inverseConstantConstraint.values.contains(value));
            return copy;
        }
        if (constraint instanceof OpenConstraint) {
            return constraint;
        }
        if (constraint instanceof ConstantConstraint constantConstraint) {
            InverseConstantConstraint copy = new InverseConstantConstraint(type(), values());
            copy.values().removeIf(value -> constantConstraint.values().contains(value));
            return copy;
        }
        throw new AssertionError();
    }

    @Override
    public @NotNull Constraint not() {
        return new ConstantConstraint(type(), values());
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint) {
            return values().isEmpty();
        }
        if (constraint instanceof InverseConstantConstraint inverseConstantConstraint) {
            return inverseConstantConstraint.values().containsAll(values());
        }
        return false;
    }
}
