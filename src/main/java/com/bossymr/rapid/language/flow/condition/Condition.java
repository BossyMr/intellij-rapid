package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Condition} represents a condition which a variable must fulfill.
 */
public interface Condition {

    /**
     * Returns if the variable represented by this condition might be missing. If a variable is missing, it cannot be
     * safely used.
     *
     * @return if the variable represented by this condition might be missing.
     */
    @Contract(pure = true)
    @NotNull Optionality optionality();

    @Contract(pure = true)
    default @NotNull Condition copy() {
        return copy(optionality());
    }

    /**
     * Creates a new copy of this condition with the specified optionality.
     *
     * @param optionality the optionality.
     * @return a new copy of this condition with the specified optionality.
     */
    @Contract(pure = true)
    @NotNull Condition copy(@NotNull Optionality optionality);

    /**
     * Returns a new condition which is the opposite to this condition.
     *
     * @return a new condition which is the opposite to this condition.
     */
    @Contract(pure = true)
    @NotNull Condition negate();

    /**
     * Returns a new condition which is the intersection of this condition and the specified condition.
     *
     * @param condition the condition.
     * @return a new condition which is the intersection of this condition and the specified condition.
     */
    @Contract(pure = true)
    @NotNull Condition and(@NotNull Condition condition);

    /**
     * Returns a new condition which is the union of this condition and the specified condition.
     *
     * @param condition the condition.
     * @return a new condition which is the union of this condition and the specified condition.
     */
    @Contract(pure = true)
    @NotNull Condition or(@NotNull Condition condition);

    /**
     * Checks if this condition contains the specified condition. This condition contains another condition if every
     * value which matches the specified condition also matches this condition.
     *
     * @param condition the condition.
     * @return if this condition contains the specified condition.
     */
    @Contract(pure = true)
    default boolean contains(@NotNull Condition condition) {
        Condition union = or(condition);
        return union.equals(this);
    }
}
