package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code OpenConstraint} represents a constraint where the value might be any valid value.
 *
 * @param type the type of value.
 */
public record OpenConstraint(@NotNull RapidType type) implements Constraint {

    public OpenConstraint {
        if (!RapidType.STRING.isAssignable(type) && type.getValueType() != ValueType.NON_VALUE_TYPE) {
            throw new AssertionError();
        }
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof MissingConstraint) {
            return constraint;
        }
        return this;
    }

    @Override
    public @NotNull Constraint not() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint) {
            return true;
        }
        if (constraint instanceof InverseConstantConstraint constantConstraint && constantConstraint.values().isEmpty()) {
            return true;
        }
        return !(constraint instanceof MissingConstraint);
    }
}
