package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A {@code RangeConstraint} represents a constraint where a value can be within the range.
 */
public record RangeConstraint(@NotNull RapidType type, @NotNull Bound lower, @NotNull Bound upper) implements Constraint {

    public RangeConstraint {
        if (!RapidType.NUMBER.isAssignable(type) || !RapidType.DOUBLE.isAssignable(type)) {
            throw new AssertionError();
        }
    }

    @Override
    public @NotNull Constraint not() {
        List<Constraint> constraints = new ArrayList<>();
        if (lower.value() != Double.NEGATIVE_INFINITY) {
            constraints.add(new RangeConstraint(type(), new Bound(true, Double.NEGATIVE_INFINITY), new Bound(!lower.inclusive(), lower.value())));
        }
        if (upper.value() != Double.POSITIVE_INFINITY) {
            constraints.add(new RangeConstraint(type(), new Bound(!upper.inclusive(), upper.value()), new Bound(true, Double.POSITIVE_INFINITY)));
        }
        return Constraint.or(constraints);
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (!(type().isAssignable(constraint.type()))) {
            throw new IllegalArgumentException();
        }
        if (constraint instanceof RangeConstraint range) {
            if (range.lower().value() < lower.value()) {
                return false;
            }
            if (range.lower().value() == lower.value) {
                if (!(lower.inclusive()) && range.lower().inclusive()) {
                    return false;
                }
            }
            if (range.upper().value() > upper.value()) {
                return false;
            }
            if (range.upper().value() == upper.value) {
                if (!(upper.inclusive()) && range.upper().inclusive()) {
                    return false;
                }
            }
        }
        if (constraint instanceof OpenConstraint) {
            return lower.value() == Double.NEGATIVE_INFINITY && upper.value() == Double.POSITIVE_INFINITY;
        }
        if (constraint instanceof InverseValueConstraint inverseValueConstraint) {
            Set<Object> values = inverseValueConstraint.values();
            for (Object value : values) {
                if (!(value instanceof Number number)) {
                    throw new IllegalArgumentException();
                }
                if (number.doubleValue() < lower.value()) {
                    return true;
                }
                if (number.doubleValue() == lower.value()) {
                    return !(lower.inclusive());
                }
                if (number.doubleValue() == upper.value()) {
                    return !(upper.inclusive());
                }
                if (number.doubleValue() > upper.value()) {
                    return true;
                }
            }
            return false;
        }
        if (constraint instanceof ValueConstraint valueConstraint) {
            Set<Object> values = valueConstraint.values();
            for (Object value : values) {
                if (!(value instanceof Number number)) {
                    throw new IllegalArgumentException();
                }
                if (number.doubleValue() < lower.value()) {
                    return false;
                }
                if (number.doubleValue() == lower.value()) {
                    return lower.inclusive();
                }
                if (number.doubleValue() == upper.value()) {
                    return upper.inclusive();
                }
                if (number.doubleValue() > upper.value()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * A {@code Bound} represents a boundary in a {@link RangeConstraint}.
     *
     * @param inclusive whether the boundary is inclusive.
     * @param value the boundary value.
     */
    public record Bound(boolean inclusive, double value) {}
}
