package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@code Constraint} represents the possible value of a variable.
 */
public interface Constraint {

    @Contract(pure = true)
    static @NotNull Constraint or(@NotNull List<Constraint> constraints) {
        if (constraints.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Constraint constraint = constraints.get(0);
        for (int i = 1; i < constraints.size(); i++) {
            constraint = constraint.or(constraints.get(i));
        }
        return constraint;
    }

    /**
     * Returns the type of the value.
     *
     * @return the type of the value.
     */
    @NotNull RapidType getType();

    /**
     * Creates a new constraint which represents the union of this constraint and the specified constraint.
     *
     * @param constraint the constraint.
     * @return the union of this constraint and the specified constraint.
     */
    @Contract(pure = true)
    @NotNull
    Constraint or(@NotNull Constraint constraint);

    /**
     * Creates a new constraint which represents the opposite of this constraint.
     *
     * @return the opposite of this constraint.
     */
    @NotNull Constraint not();

    /**
     * Checks whether this constraint contains the specified constraint.
     * <p>
     * The constraint {@code 0 < x < 10} contains the constraint {@code 2 < x < 3}, but does not contain the constraint
     * {@code 12 < x < 13}.
     *
     * @param constraint the constraint.
     * @return whether this constraint contains the specified constraint.
     */
    boolean contains(@NotNull Constraint constraint);

}
