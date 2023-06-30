package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidAtomic;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * A {@code Constraint} represents a condition which a variable must fulfill.
 */
public interface Constraint {

    static @NotNull Constraint getTopConstraint(@NotNull RapidType type) {
        if (type.isAssignable(RapidType.NUMBER)) {
            return NumericConstraint.any();
        }
        if (type.isAssignable(RapidType.STRING)) {
            return new InverseStringConstraint(Optionality.PRESENT, new HashSet<>());
        }
        if (type.isAssignable(RapidType.BOOLEAN)) {
            return BooleanConstraint.any();
        }
        RapidStructure targetStructure = type.getTargetStructure();
        if (targetStructure instanceof RapidAtomic) {
            return new OpenConstraint(Optionality.PRESENT);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns if the variable represented by this condition might be missing. If a variable is missing, it cannot be
     * safely used.
     *
     * @return if the variable represented by this condition might be missing.
     */
    @Contract(pure = true)
    @NotNull Optionality getOptionality();

    @Contract(pure = true)
    default @NotNull Constraint copy() {
        return copy(getOptionality());
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
