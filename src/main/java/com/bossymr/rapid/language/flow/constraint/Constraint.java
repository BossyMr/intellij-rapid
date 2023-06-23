package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@code Constraint} represents the possible value of a variable.
 */
public interface Constraint {

    static @NotNull Constraint any(@NotNull RapidType type) {
        if (RapidType.DOUBLE.isAssignable(type) || RapidType.NUMBER.isAssignable(type)) {
            return new RangeConstraint(type, new RangeConstraint.Bound(true, Double.MIN_VALUE), new RangeConstraint.Bound(true, Double.MAX_VALUE));
        }
        if (RapidType.BOOLEAN.isAssignable(type)) {
            return new ConstantConstraint(type, Set.of(true, false));
        }
        if (RapidType.STRING.isAssignable(type)) {
            return new OpenConstraint(type);
        }
        if (type.getTargetStructure() instanceof RapidRecord record) {
            Map<String, Constraint> constraints = new HashMap<>();
            for (RapidComponent component : record.getComponents()) {
                RapidType componentType = Objects.requireNonNull(component.getType());
                constraints.put(component.getName(), Constraint.any(componentType));
            }
            return new RecordConstraint(type, constraints);
        }
        return new OpenConstraint(type);
    }

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
