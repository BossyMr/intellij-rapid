package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ValueConstraint(@NotNull RapidType type, @NotNull Set<Object> values) implements Constraint {

    @Override
    public @NotNull Constraint not() {
        return new InverseValueConstraint(type(), values());
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (constraint instanceof ValueConstraint valueConstraint) {
            return values().containsAll(valueConstraint.values());
        }
        if (constraint instanceof RangeConstraint rangeConstraint) {
            if (!(type().isAssignable(rangeConstraint.type()))) {
                return false;
            }
            if (rangeConstraint.lower().inclusive() && rangeConstraint.upper().inclusive()) {
                if (rangeConstraint.lower().value() == rangeConstraint.upper().value()) {
                    return values.contains(rangeConstraint.lower().value());
                }
            }
        }
        if (constraint instanceof ClosedConstraint) {
            return values().isEmpty();
        }
        return false;
    }
}
