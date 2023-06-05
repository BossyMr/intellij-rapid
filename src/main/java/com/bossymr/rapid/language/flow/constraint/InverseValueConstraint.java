package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record InverseValueConstraint(@NotNull RapidType type, @NotNull Set<Object> values) implements Constraint {

    @Override
    public @NotNull Constraint not() {
        return new ValueConstraint(type(), values());
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (!(type().isAssignable(constraint.type()))) {
            return false;
        }
        if (constraint instanceof ValueConstraint valueConstraint) {
            Set<Object> objects = valueConstraint.values();
            return values().stream().noneMatch(objects::contains);
        }
        if (constraint instanceof RangeConstraint rangeConstraint) {
            for (Object value : values()) {
                if (!(value instanceof Number number)) {
                    throw new IllegalArgumentException();
                }
                if (number.doubleValue() < rangeConstraint.lower().value()) {
                    return true;
                }
                if (number.doubleValue() == rangeConstraint.lower().value()) {
                    return !(rangeConstraint.lower().inclusive());
                }
                if (number.doubleValue() == rangeConstraint.upper().value()) {
                    return !(rangeConstraint.upper().inclusive());
                }
                if (number.doubleValue() > rangeConstraint.upper().value()) {
                    return true;
                }
            }
            return false;
        }
        if (constraint instanceof OpenConstraint) {
            return values().isEmpty();
        }
        return false;
    }
}
