package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A {@code RangeConstraint} represents a constraint where the value might be any value in a specific range.
 */
public final class RangeConstraint implements Constraint {

    private final @NotNull RapidType type;
    private final @NotNull List<Range> ranges = new ArrayList<>();

    public RangeConstraint(@NotNull RapidType type, @NotNull Bound lower, @NotNull Bound upper) {
        this(type);
        add(new Range(lower, upper));
    }

    public RangeConstraint(@NotNull RapidType type) {
        if (!RapidType.NUMBER.isAssignable(type) && !RapidType.DOUBLE.isAssignable(type)) {
            throw new AssertionError();
        }
        this.type = type;
    }

    public void add(@NotNull Range range) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).intersects(range)) {
                ranges.set(i, ranges.get(i).combine(range));
                return;
            }
            if (range.upper().value() >= ranges.get(i).lower().value()) {
                ranges.add(i, range);
                return;
            }
        }
        ranges.add(range);
    }

    private void remove(@NotNull Range range) {
        ListIterator<Range> iterator = ranges.listIterator();
        while (iterator.hasNext()) {
            Range next = iterator.next();
            if (next.intersects(range)) {
                Range beforeMask = calculateRangeBeforeMask(next, range);
                Range afterMask = calculateRangeAfterMask(next, range);
                iterator.remove();
                if (beforeMask != null) {
                    iterator.add(beforeMask);
                }
                if (afterMask != null) {
                    iterator.add(afterMask);
                }
            }
        }
    }

    private @Nullable Range calculateRangeBeforeMask(@NotNull Range original, @NotNull Range mask) {
        if (original.lower().value() > mask.lower().value()) {
            return null;
        }
        if (original.lower().value() == mask.lower().value()) {
            if (original.lower().inclusive() && !mask.lower().inclusive()) {
                return new Range(new Bound(true, original.lower().value()), new Bound(true, original.lower().value()));
            } else {
                return null;
            }
        }
        return new Range(original.lower(), new Bound(!mask.lower().inclusive(), mask.lower().value()));
    }

    private @Nullable Range calculateRangeAfterMask(@NotNull Range original, @NotNull Range mask) {
        if (original.upper().value() < mask.upper().value()) {
            return null;
        }
        if (original.upper().value() == mask.upper().value()) {
            if (original.upper().inclusive() && !mask.upper().inclusive()) {
                return new Range(new Bound(true, original.upper().value()), new Bound(true, original.upper().value()));
            } else {
                return null;
            }
        }
        return new Range(new Bound(!mask.upper().inclusive(), mask.upper().value()), original.upper());
    }

    public @NotNull List<Range> getRanges() {
        return ranges;
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof MissingConstraint missingConstraint) {
            return new MissingConstraint(getType(), or(missingConstraint.constraint()));
        }
        if (!(constraint instanceof RangeConstraint rangeConstraint)) {
            throw new IllegalArgumentException();
        }
        RangeConstraint copy = new RangeConstraint(getType());
        getRanges().forEach(copy::add);
        for (Range range : rangeConstraint.getRanges()) {
            copy.add(range);
        }
        return copy;
    }

    @Override
    public @NotNull Constraint not() {
        RangeConstraint rangeConstraint = new RangeConstraint(getType(), new Bound(true, Double.MIN_VALUE), new Bound(true, Double.MAX_VALUE));
        for (Range range : getRanges()) {
            rangeConstraint.remove(range);
        }
        return rangeConstraint;
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (!(constraint instanceof RangeConstraint rangeConstraint)) {
            throw new IllegalArgumentException();
        }
        return rangeConstraint.getRanges().stream().allMatch(value -> getRanges().stream().anyMatch(range -> range.contains(value)));
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeConstraint that = (RangeConstraint) o;
        return Objects.equals(type, that.type) && Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, ranges);
    }

    @Override
    public String toString() {
        return "RangeConstraint{" +
                "type=" + type +
                ", ranges=" + ranges +
                '}';
    }

    public record Range(@NotNull Bound lower, @NotNull Bound upper) {

        public Range {
            if (lower.value() > upper.value()) {
                throw new IllegalArgumentException("Lower bound: " + lower().value() + " must be smaller compared to the upper bound: " + upper().value());
            }
            if (lower.value() == upper.value()) {
                if (!lower().inclusive() || !upper().inclusive()) {
                    throw new IllegalArgumentException("Range: " + this + " must include at least one point");
                }
            }
        }

        public boolean contains(@NotNull Range range) {
            return contains(range.lower()) && contains(range.upper());
        }

        public boolean intersects(@NotNull Range range) {
            return contains(range.lower()) || contains(range.upper()) || range.contains(lower()) || range.contains(upper());
        }

        private boolean contains(@NotNull Bound point) {
            if (point.value() > lower().value() || point.value() < upper().value()) {
                return true;
            } else if (point.value() == lower().value()) {
                return point.inclusive() && lower().inclusive();
            } else if (point.value() == upper().value()) {
                return point.inclusive() && upper.inclusive();
            } else {
                throw new AssertionError();
            }
        }

        public @NotNull Range combine(@NotNull Range range) {
            Bound lower, upper;
            if (lower().value() < range.lower().value()) {
                lower = lower();
            } else if (lower().value() > range.lower().value()) {
                lower = range.lower();
            } else if (lower().value() == range.lower().value()) {
                lower = new Bound(lower().inclusive() || range.lower().inclusive(), lower().value());
            } else {
                throw new AssertionError();
            }
            if (upper().value() > range.upper().value()) {
                upper = upper();
            } else if (upper().value() < range.upper().value()) {
                upper = range.upper();
            } else if (upper().value() == range.upper().value()) {
                upper = new Bound(upper().inclusive() || range.upper().inclusive(), upper().value());
            } else {
                throw new AssertionError();
            }
            return new Range(lower, upper);
        }
    }

    /**
     * A {@code Bound} represents a boundary in a {@link RangeConstraint}.
     *
     * @param inclusive whether the boundary is inclusive.
     * @param value the boundary value.
     */
    public record Bound(boolean inclusive, double value) {}
}
