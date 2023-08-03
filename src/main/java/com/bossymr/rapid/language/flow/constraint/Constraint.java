package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidAtomic;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@code Constraint} represents a condition which a variable must fulfill.
 */
public interface Constraint {

    static @NotNull Collector<Constraint, List<Constraint>, Constraint> or(@NotNull RapidType type) {
        return of(Constraint::or, () -> Constraint.none(type));
    }

    static @NotNull Collector<Constraint, List<Constraint>, Constraint> and(@NotNull RapidType type) {
        return of(Constraint::and, () -> Constraint.any(type));
    }

    private static @NotNull Collector<Constraint, List<Constraint>, Constraint> of(@NotNull Function<List<Constraint>, Constraint> mapper, @NotNull Supplier<Constraint> empty) {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, constraints -> constraints.isEmpty() ? empty.get() : mapper.apply(constraints));
    }

    /**
     * Creates a new {@code Constraint} which will match if any of the specified constraints match.
     *
     * @param constraints the constraints.
     * @return the constraint.
     */
    static @NotNull Constraint or(@NotNull Collection<? extends Constraint> constraints) {
        if (constraints.isEmpty()) {
            throw new IllegalStateException();
        }
        Iterator<? extends Constraint> iterator = constraints.iterator();
        Constraint constraint = iterator.next();
        while (iterator.hasNext()) {
            constraint = constraint.or(iterator.next());
        }
        return constraint;
    }

    static @NotNull Constraint and(@NotNull Collection<? extends Constraint> constraints) {
        if (constraints.isEmpty()) {
            throw new IllegalStateException();
        }
        Iterator<? extends Constraint> iterator = constraints.iterator();
        Constraint constraint = iterator.next();
        while (iterator.hasNext()) {
            constraint = constraint.and(iterator.next());
        }
        return constraint;
    }

    /**
     * Creates a new {@code Constraint} which will match any valid value.
     *
     * @param type the type of the value.
     * @return the constraint.
     */
    static @NotNull Constraint any(@NotNull RapidType type) {
        return any(type, Optionality.PRESENT);
    }

    static @NotNull Constraint any(@NotNull RapidType type, @NotNull Optionality optionality) {
        if (type.equals(RapidType.ANYTYPE)) {
            return new OpenConstraint(optionality);
        }
        if (type.isAssignable(RapidType.NUMBER)) {
            return new NumericConstraint(optionality, NumericConstraint.Bound.MIN_VALUE, NumericConstraint.Bound.MAX_VALUE);
        }
        if (type.isAssignable(RapidType.STRING)) {
            return new InverseStringConstraint(optionality, new HashSet<>());
        }
        if (type.isAssignable(RapidType.BOOLEAN)) {
            return BooleanConstraint.any(optionality);
        }
        RapidStructure targetStructure = type.getTargetStructure();
        if (targetStructure instanceof RapidAtomic) {
            return new OpenConstraint(Optionality.PRESENT);
        }
        return new OpenConstraint(optionality);
    }

    static @NotNull Constraint none(@NotNull RapidType type) {
        return any(type).negate();
    }

    /**
     * Returns if the variable represented by this condition might be missing. If a variable is missing, it cannot be
     * safely used.
     *
     * @return if the variable represented by this condition might be missing.
     */
    @Contract(pure = true)
    @NotNull Optionality getOptionality();

    /**
     * Creates a copy of this constraint with the specified optionality.
     *
     * @param optionality the optionality.
     * @return a copy of this constraint.
     */
    @Contract(pure = true)
    @NotNull Constraint setOptionality(@NotNull Optionality optionality);

    @NotNull Optional<?> getValue();

    /**
     * Returns a new condition which is the opposite to this condition.
     *
     * @return a new condition which is the opposite to this condition.
     */
    @Contract(pure = true)
    @NotNull Constraint negate();

    /**
     * Returns a new constraint which is the intersection of this constraint and the specified constraint.
     *
     * @param constraint the constraint.
     * @return a new constraint which is the intersection of this constraint and the specified constraint.
     */
    @Contract(pure = true)
    @NotNull Constraint and(@NotNull Constraint constraint);

    /**
     * Returns a new constraint which is the union of this constraint and the specified constraint.
     *
     * @param constraint the constraint.
     * @return a new constraint which is the union of this constraint and the specified constraint.
     */
    @Contract(pure = true)
    @NotNull Constraint or(@NotNull Constraint constraint);

    /**
     * Checks whether this constraint will match every valid value.
     *
     * @return whether this constraint will match every valid value.
     * @see #isEmpty()
     */
    @Contract(pure = true)
    boolean isFull();

    /**
     * Checks whether this constraint will not match any valid value.
     *
     * @return whether this constraint will not match any valid value.
     * @see #isFull()
     */
    @Contract(pure = true)
    boolean isEmpty();

    /**
     * Checks if this constraint contains the specified constraint. This constraint contains another constraint if every
     * value that matches the specified constraint also matches this constraint.
     *
     * @param constraint the constraint.
     * @return if this constraint contains the specified constraint.
     */
    @Contract(pure = true)
    default boolean contains(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint) {
            return false;
        }
        if (!(this.getClass().isInstance(constraint))) {
            return false;
        }
        if (getOptionality() == Optionality.PRESENT) {
            if (constraint.getOptionality() == Optionality.MISSING) {
                return false;
            }
        }
        if (getOptionality() == Optionality.MISSING) {
            if (constraint.getOptionality() == Optionality.PRESENT) {
                return false;
            }
        }
        Constraint union;
        try {
            union = or(constraint);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return union.equals(this);
    }

    /**
     * Checks if this constraint intersects with the specified constraint.
     *
     * @param constraint the constraint.
     * @return if this constraint intersects with the specified constraint.
     */
    @Contract(pure = true)
    default boolean intersects(@NotNull Constraint constraint) {
        if (getOptionality() == Optionality.PRESENT) {
            if (constraint.getOptionality() == Optionality.MISSING) {
                return false;
            }
        }
        if (getOptionality() == Optionality.MISSING) {
            if (constraint.getOptionality() == Optionality.PRESENT) {
                return false;
            }
        }
        Constraint intersection;
        try {
            intersection = and(constraint);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return !(intersection.isEmpty());
    }

    @NotNull String getPresentableText();
}
