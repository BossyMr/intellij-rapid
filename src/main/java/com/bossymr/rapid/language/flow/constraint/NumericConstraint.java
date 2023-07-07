package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@code RangeCondition} represents a condition where a variable might be equal to any value in the range.
 */
public class NumericConstraint implements Constraint {

    private final @NotNull Optionality optionality;
    private final @NotNull List<Range> ranges;

    public NumericConstraint(@NotNull Optionality optionality, @NotNull Bound lower, @NotNull Bound upper) {
        this(optionality);
        ranges.add(new Range(lower, upper));
    }

    public NumericConstraint(@NotNull Optionality optionality, @NotNull List<Range> ranges) {
        this.optionality = optionality;
        this.ranges = ranges;
    }

    public NumericConstraint(@NotNull Optionality optionality) {
        this.optionality = optionality;
        this.ranges = new ArrayList<>();
    }

    public static @NotNull NumericConstraint any() {
        return new NumericConstraint(Optionality.PRESENT, Bound.MIN_VALUE, Bound.MAX_VALUE);
    }

    public static @NotNull NumericConstraint equalTo(double value) {
        return new NumericConstraint(Optionality.PRESENT, new Bound(true, value), new Bound(true, value));
    }

    public static @NotNull NumericConstraint greaterThan(double value) {
        return new NumericConstraint(Optionality.PRESENT, new Bound(false, value), Bound.MAX_VALUE);
    }

    public static @NotNull NumericConstraint greaterThanOrEqual(double value) {
        return new NumericConstraint(Optionality.PRESENT, new Bound(true, value), Bound.MAX_VALUE);
    }

    public static @NotNull NumericConstraint lessThan(double value) {
        return new NumericConstraint(Optionality.PRESENT, Bound.MIN_VALUE, new Bound(false, value));
    }

    public static @NotNull NumericConstraint lessThanOrEqual(double value) {
        return new NumericConstraint(Optionality.PRESENT, Bound.MIN_VALUE, new Bound(true, value));
    }

    public @NotNull List<Range> getRanges() {
        return ranges;
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull Bound getMinimum() {
        return ranges.get(0).lower();
    }

    public @NotNull Bound getMaximum() {
        return ranges.get(ranges.size() - 1).upper();
    }

    public @Nullable Double getPoint() {
        if (ranges.size() != 1) {
            return null;
        }
        Range range = ranges.get(0);
        if (range.lower().isInclusive() && range.upper().isInclusive()) {
            if (range.lower().value() == range.upper().value()) {
                return range.lower().value();
            }
        }
        return null;
    }

    /**
     * Adds the specified range to this constraint, so that this constraint will be fulfilled if either this constraint
     * or the specified range is fulfilled.
     *
     * @param range the range.
     */
    private void union(@NotNull Range range) {
        ListIterator<Range> iterator = this.ranges.listIterator();
        while (iterator.hasNext()) {
            Range next = iterator.next();
            if (next.intersects(range)) {
                iterator.set(next.union(range));
                return;
            }
            if (range.upper().value() >= next.lower().value()) {
                iterator.add(range);
                return;
            }
        }
        this.ranges.add(range);
    }

    /**
     * Adds the specified ranges to this constraint, so that this constraint will be fulfilled if both this constraint
     * and any of the specified ranges are fulfilled.
     *
     * @param ranges the ranges.
     */
    private void intersect(@NotNull List<Range> ranges) {
        ListIterator<Range> iterator = this.ranges.listIterator();
        while (iterator.hasNext()) {
            Range next = iterator.next();
            List<Range> intersects = ranges.stream()
                    .filter(range -> range.intersects(next))
                    .map(range -> range.intersect(next))
                    .toList();
            if (intersects.isEmpty()) {
                iterator.remove();
            } else {
                intersects.forEach(iterator::add);
            }
        }
    }

    @Contract(pure = true)
    private @NotNull NumericConstraint negate(@NotNull Range range) {
        NumericConstraint condition = new NumericConstraint(getOptionality());
        if (!range.lower().equals(Bound.MIN_VALUE)) {
            Bound lower = range.lower();
            Range below = new Range(Bound.MIN_VALUE, new Bound(!lower.isInclusive(), lower.value()));
            condition.ranges.add(below);
        }
        if (!range.upper().equals(Bound.MAX_VALUE)) {
            Bound upper = range.upper();
            Range above = new Range(new Bound(!upper.isInclusive(), upper.value()), Bound.MAX_VALUE);
            condition.ranges.add(above);
        }
        return condition;
    }

    @Override
    public @NotNull NumericConstraint negate() {
        NumericConstraint condition = new NumericConstraint(getOptionality(), Bound.MIN_VALUE, Bound.MAX_VALUE);
        for (Range range : ranges) {
            condition = condition.and(negate(range));
        }
        return condition;
    }

    @Override
    public @NotNull NumericConstraint and(@NotNull Constraint constraint) {
        if (!(constraint instanceof NumericConstraint numericCondition)) {
            throw new IllegalArgumentException();
        }
        NumericConstraint copy = new NumericConstraint(getOptionality().combine(constraint.getOptionality()), new ArrayList<>(ranges));
        copy.intersect(numericCondition.ranges);
        return copy;
    }

    @Override
    public @NotNull NumericConstraint or(@NotNull Constraint constraint) {
        if (!(constraint instanceof NumericConstraint numericCondition)) {
            throw new IllegalArgumentException();
        }
        NumericConstraint copy = new NumericConstraint(getOptionality().combine(constraint.getOptionality()), new ArrayList<>(ranges));
        for (Range range : numericCondition.ranges) {
            copy.union(range);
        }
        return copy;
    }

    public record Range(@NotNull Bound lower, @NotNull Bound upper) {

        public Range {
            if (lower.value() > upper.value()) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Checks whether this range encompasses the specified range.
         *
         * @param range the range.
         * @return whether this range encompasses the specified range.
         */
        public boolean contains(@NotNull Range range) {
            return contains(range.lower()) && contains(range.upper());
        }

        /**
         * Checks whether this range intersects, shares any point, with the specified range.
         *
         * @param range the range.
         * @return whether this range intersects the specified range.
         */
        public boolean intersects(@NotNull Range range) {
            return contains(range.lower()) || contains(range.upper()) || range.contains(lower()) || range.contains(upper());
        }

        private boolean contains(@NotNull Bound point) {
            if (lower().value() < point.value() && upper().value() > point.value()) {
                return true;
            } else if (point.value() == lower().value()) {
                return point.isInclusive() && lower().isInclusive();
            } else if (point.value() == upper().value()) {
                return point.isInclusive() && upper().isInclusive();
            } else {
                return false;
            }
        }

        public @NotNull Range intersect(@NotNull Range range) {
            Bound lowerBound = getUpperbound(lower(), range.lower(), lower().isInclusive() && range.lower().isInclusive());
            Bound upperbound = getLowerBound(upper(), range.upper(), upper().isInclusive() && range.upper().isInclusive());
            return new Range(lowerBound, upperbound);
        }

        /**
         * Creates a new range which combines both this range and the specified range.
         *
         * @param range the range.
         * @return a new range which encompasses both this and the specified range.
         */
        public @NotNull Range union(@NotNull Range range) {
            Bound lowerBound = getLowerBound(lower(), range.lower(), lower().isInclusive() || range.lower().isInclusive());
            Bound upperbound = getUpperbound(upper(), range.upper(), upper().isInclusive() || range.upper().isInclusive());
            return new Range(lowerBound, upperbound);
        }

        private @NotNull Bound getUpperbound(@NotNull Bound thisUpper, @NotNull Bound otherUpper, boolean isInclusive) {
            if (thisUpper.value() > otherUpper.value()) {
                return thisUpper;
            } else if (thisUpper.value() < otherUpper.value()) {
                return otherUpper;
            } else if (thisUpper.value() == otherUpper.value()) {
                return new Bound(isInclusive, thisUpper.value());
            }
            throw new AssertionError();
        }

        private @NotNull Bound getLowerBound(@NotNull Bound thisLower, @NotNull Bound otherLower, boolean isInclusive) {
            if (thisLower.value() < otherLower.value()) {
                return thisLower;
            } else if (thisLower.value() > otherLower.value()) {
                return otherLower;
            } else if (thisLower.value() == otherLower.value()) {
                return new Bound(isInclusive, thisLower.value());
            }
            throw new AssertionError();
        }
    }

    /**
     * A {@code Bound} represents the boundary of a range.
     *
     * @param isInclusive whether the boundary is inclusive.
     * @param value the boundary value.
     */
    public record Bound(boolean isInclusive, double value) {

        public static @NotNull Bound MAX_VALUE = new Bound(true, 2 ^ 52);
        public static @NotNull Bound MIN_VALUE = new Bound(true, -2 ^ 52);

    }
}
