package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Constraint} represents a condition which a variable must fulfill.
 */
public interface Constraint {

    /**
     * Returns if the variable represented by this condition might be missing. If a variable is missing, it cannot be
     * safely used.
     *
     * @return if the variable represented by this condition might be missing.
     */
    @Contract(pure = true)
    @NotNull Optionality optionality();

    @Contract(pure = true)
    default @NotNull Constraint copy() {
        return copy(optionality());
    }

    /**
     * Creates a new copy of this condition with the specified optionality.
     *
     * @param optionality the optionality.
     * @return a new copy of this condition with the specified optionality.
     */
    @Contract(pure = true)
    @NotNull Constraint copy(@NotNull Optionality optionality);

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
     * Checks if this constraint contains the specified constraint. This constraint contains another constraint if every
     * value which matches the specified constraint also matches this constraint.
     *
     * @param constraint the constraint.
     * @return if this constraint contains the specified constraint.
     */
    @Contract(pure = true)
    default boolean contains(@NotNull Constraint constraint) {
        Constraint union = or(constraint);
        return union.equals(this);
    }
}
