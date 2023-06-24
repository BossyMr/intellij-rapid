package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@code RangeCondition} represents a condition where a variable might be equal to any value in the range.
 */
public class NumericCondition implements Condition {

    private final @NotNull Optionality optionality;
    private final @NotNull List<Range> ranges;

    public NumericCondition(@NotNull Optionality optionality, @NotNull Bound lower, @NotNull Bound upper) {
        this(optionality);
        ranges.add(new Range(lower, upper));
    }

    public NumericCondition(@NotNull Optionality optionality, @NotNull List<Range> ranges) {
        this.optionality = optionality;
        this.ranges = ranges;
    }

    public NumericCondition(@NotNull Optionality optionality) {
        this.optionality = optionality;
        this.ranges = new ArrayList<>();
    }

    @Override
    public @NotNull Optionality optionality() {
        return optionality;
    }

    @Override
    public @NotNull NumericCondition copy(@NotNull Optionality optionality) {
        return new NumericCondition(optionality, new ArrayList<>(ranges));
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
    private @NotNull NumericCondition negate(@NotNull Range range) {
        NumericCondition condition = new NumericCondition(optionality());
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
    public @NotNull NumericCondition negate() {
        NumericCondition condition = new NumericCondition(optionality(), Bound.MIN_VALUE, Bound.MAX_VALUE);
        for (Range range : ranges) {
            condition = condition.and(negate(range));
        }
        return condition;
    }

    @Override
    public @NotNull NumericCondition and(@NotNull Condition condition) {
        if (!(condition instanceof NumericCondition numericCondition)) {
            throw new IllegalArgumentException();
        }
        NumericCondition copy = new NumericCondition(optionality(), new ArrayList<>(ranges));
        copy.intersect(numericCondition.ranges);
        return copy;
    }

    @Override
    public @NotNull NumericCondition or(@NotNull Condition condition) {
        if (!(condition instanceof NumericCondition numericCondition)) {
            throw new IllegalArgumentException();
        }
        NumericCondition copy = new NumericCondition(optionality(), new ArrayList<>(ranges));
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
