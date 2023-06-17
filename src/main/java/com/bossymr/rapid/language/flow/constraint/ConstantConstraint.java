package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@code ConstantConstraint} is a constraint where the value is guaranteed to be one of the specified values.
 *
 * @param type the type of value.
 * @param values the values which the value can be.
 */
public record ConstantConstraint(@NotNull RapidType type, @NotNull Set<Object> values) implements Constraint {

    public ConstantConstraint(@NotNull RapidType type, @NotNull Set<Object> values) {
        if (!RapidType.STRING.isAssignable(type) && !RapidType.BOOLEAN.isAssignable(type)) {
            throw new AssertionError();
        }
        this.values = new HashSet<>(values);
        this.type = type;
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint) {
            return constraint;
        }
        if (constraint instanceof ConstantConstraint constantConstraint) {
            ConstantConstraint copy = new ConstantConstraint(getType(), values());
            copy.values().addAll(constantConstraint.values());
            return copy;
        }
        throw new AssertionError();
    }

    @Override
    public @NotNull Constraint not() {
        return new InverseConstantConstraint(getType(), values());
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (constraint instanceof InverseConstantConstraint) {
            return false;
        }
        if (constraint instanceof ConstantConstraint constantConstraint) {
            return values().containsAll(constantConstraint.values());
        }
        return false;
    }
}
