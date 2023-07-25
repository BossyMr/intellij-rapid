package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    @Override
    public @NotNull Constraint setOptionality(@NotNull Optionality optionality) {
        return new NumericConstraint(optionality, new ArrayList<>(ranges));
    }

    public @NotNull Optional<Range> getRange() {
        Optional<Bound> minimum = getMinimum();
        Optional<Bound> maximum = getMaximum();
        if (minimum.isEmpty() || maximum.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Range(minimum.orElseThrow(), maximum.orElseThrow()));
    }

    @Override
    public @NotNull Optional<Double> getValue() {
        return getPoint();
    }

    public @NotNull Optional<Bound> getMinimum() {
        if (ranges.isEmpty()) {
            return Optional.empty();
        }
        Range range = ranges.get(0);
        return Optional.of(range.lower());
    }

    public @NotNull Optional<Bound> getMaximum() {
        if (ranges.isEmpty()) {
            return Optional.empty();
        }
        Range range = ranges.get(ranges.size() - 1);
        return Optional.of(range.upper());
    }

    public @NotNull Optional<Double> getPoint() {
        if (ranges.size() != 1) {
            return Optional.empty();
        }
        Range range = ranges.get(0);
        if (range.lower().isInclusive() && range.upper().isInclusive()) {
            if (range.lower().value() == range.upper().value()) {
                return Optional.of(range.lower().value());
            }
        }
        return Optional.empty();
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
            iterator.remove();
            intersects.forEach(iterator::add);
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
    public @NotNull Constraint negate() {
        Constraint constraint = new NumericConstraint(getOptionality(), Bound.MIN_VALUE, Bound.MAX_VALUE);
        for (Range range : ranges) {
            constraint = constraint.and(negate(range));
        }
        return constraint;
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.and(this);
        }
        if (!(constraint instanceof NumericConstraint numericCondition)) {
            throw new IllegalArgumentException("Cannot create intersection of: " + this + " and " + constraint);
        }
        NumericConstraint copy = new NumericConstraint(getOptionality().and(constraint.getOptionality()), new ArrayList<>(ranges));
        copy.intersect(numericCondition.ranges);
        return copy;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.or(this);
        }
        if (!(constraint instanceof NumericConstraint numericCondition)) {
            throw new IllegalArgumentException("Cannot create union of: " + this + " and " + constraint);
        }
        NumericConstraint copy = new NumericConstraint(getOptionality().or(constraint.getOptionality()), new ArrayList<>(ranges));
        for (Range range : numericCondition.ranges) {
            copy.union(range);
        }
        return copy;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericConstraint that = (NumericConstraint) o;
        return optionality == that.optionality && Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionality, ranges);
    }

    @Override
    public String toString() {
        return "NumericConstraint{" +
                "optionality=" + optionality +
                ", ranges=" + ranges +
                '}';
    }

    public record Range(@NotNull Bound lower, @NotNull Bound upper) {

        public static @NotNull Range MAXIMUM_RANGE = new Range(Bound.MIN_VALUE, Bound.MAX_VALUE);

        public Range {
            if (lower.value() > upper.value()) {
                throw new IllegalArgumentException("Upper bound must be larger than lower bound");
            }
            if (lower.value() == upper.value()) {
                if (!(lower.isInclusive() && upper.isInclusive())) {
                    throw new IllegalArgumentException("Range must span at least one point");
                }
            }
        }

        public @NotNull Optional<Double> getPoint() {
            if (lower().value() == upper().value()) {
                if (lower().isInclusive() && upper().isInclusive()) {
                    return Optional.of(lower().value());
                }
            }
            return Optional.empty();
        }

        /**
         * Checks whether this range encompasses the specified range.
         *
         * @param range the range.
         * @return whether this range encompasses the specified range.
         */
        public boolean contains(@NotNull Range range) {
            return contains(range.lower(), true) && contains(range.upper(), false);
        }

        public @NotNull Optional<Boolean> isSmaller(@NotNull Range range) {
            if (intersects(range) && !(contains(range))) {
                return Optional.empty();
            }
            if (upper().value() < range.lower().value()) {
                return Optional.of(true);
            }
            if (lower().value() < range.upper().value()) {
                return Optional.of(false);
            }
            if (upper().value() == range.lower().value()) {
                return Optional.of(!(contains(range.lower(), true)));
            }
            if (lower().value() == range.upper().value()) {
                return Optional.of(!(contains(range.upper(), false)));
            }
            return Optional.empty();
        }

        public @NotNull Optional<Boolean> isEqual(@NotNull Range range) {
            Optional<Double> point = getPoint();
            if (point.isEmpty()) {
                return Optional.empty();
            }
            Optional<Double> otherPoint = range.getPoint();
            if (otherPoint.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(point.equals(otherPoint));
        }

        /**
         * Checks whether this range intersects, shares any point, with the specified range.
         *
         * @param range the range.
         * @return whether this range intersects the specified range.
         */
        public boolean intersects(@NotNull Range range) {
            return contains(range.lower(), true) || contains(range.upper(), false) || range.contains(lower(), true) || range.contains(upper(), false);
        }

        private boolean contains(@NotNull Bound point, boolean isLower) {
            if (lower().value() < point.value() && upper().value() > point.value()) {
                return true;
            } else if (point.value() == lower().value()) {
                return lower().isInclusive() ? point.isInclusive() || isLower : !(point.isInclusive()) && isLower;
            } else if (point.value() == upper().value()) {
                return upper().isInclusive() ? point.isInclusive() || !(isLower) : !(point.isInclusive()) && !(isLower);
            } else {
                return false;
            }
        }

        /**
         * Creates a new range which encompasses the range which both this range and the specified range contain.
         *
         * @param range the range.
         * @return a new range which encompasses the intersection of this range and the specified range.
         */
        public @NotNull Range intersect(@NotNull Range range) {
            if (!(intersects(range))) {
                throw new IllegalArgumentException();
            }
            Bound lowerBound = getUpperbound(lower(), range.lower(), lower().isInclusive() && range.lower().isInclusive());
            Bound upperbound = getLowerBound(upper(), range.upper(), upper().isInclusive() && range.upper().isInclusive());
            return new Range(lowerBound, upperbound);
        }

        /**
         * Creates a new range which combines both this range and the specified range.
         *
         * @param range the range.
         * @return a new range which encompasses the union of both this and the specified range.
         */
        public @NotNull Range union(@NotNull Range range) {
            if (!(intersects(range))) {
                throw new IllegalArgumentException();
            }
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

        /**
         * The largest value for a numeric value.
         */
        public static @NotNull Bound MAX_VALUE = new Bound(true, Math.pow(2, 52));

        /**
         * The smaller value for a numeric value.
         */
        public static @NotNull Bound MIN_VALUE = new Bound(true, -Math.pow(2, 52));

        public static @NotNull Bound min(@NotNull Bound a, @NotNull Bound b) {
            if (a.value() < b.value()) {
                return a;
            }
            if (b.value() < a.value()) {
                return b;
            }
            if (a.isInclusive()) {
                return a;
            }
            if (b.isInclusive()) {
                return b;
            }
            return a;
        }

        public static @NotNull Bound max(@NotNull Bound a, @NotNull Bound b) {
            if (a.value() > b.value()) {
                return a;
            }
            if (b.value() > a.value()) {
                return b;
            }
            if (a.isInclusive()) {
                return a;
            }
            if (b.isInclusive()) {
                return b;
            }
            return a;
        }

    }
}
