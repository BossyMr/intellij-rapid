package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class BooleanConstraint implements Constraint {

    private static final @NotNull BooleanConstraint ANY_VALUE = new BooleanConstraint(BooleanValue.ANY_VALUE);

    private static final @NotNull BooleanConstraint ALWAYS_TRUE = new BooleanConstraint(BooleanValue.ANY_VALUE);

    private static final @NotNull BooleanConstraint ALWAYS_FALSE = new BooleanConstraint(BooleanValue.ANY_VALUE);

    private static final @NotNull BooleanConstraint NO_VALUE = new BooleanConstraint(BooleanValue.ANY_VALUE);

    private final @NotNull Optionality optionality;
    private final @NotNull BooleanValue value;

    public BooleanConstraint(@NotNull Optionality optionality, @NotNull BooleanValue value) {
        this.optionality = optionality;
        this.value = value;
    }

    public BooleanConstraint(@NotNull BooleanValue value) {
        this.optionality = Optionality.PRESENT;
        this.value = value;
    }

    public static @NotNull BooleanConstraint withValue(boolean value) {
        return value ? alwaysTrue() : alwaysFalse();
    }

    public static @NotNull BooleanConstraint any() {
        return ANY_VALUE;
    }

    public static @NotNull BooleanConstraint any(@NotNull Optionality optionality) {
        if (optionality == Optionality.PRESENT) {
            return ANY_VALUE;
        } else {
            return new BooleanConstraint(optionality, BooleanValue.ANY_VALUE);
        }
    }

    public static @NotNull BooleanConstraint noValue() {
        return NO_VALUE;
    }

    public static @NotNull BooleanConstraint alwaysTrue() {
        return ALWAYS_TRUE;
    }

    public static @NotNull BooleanConstraint alwaysFalse() {
        return ALWAYS_FALSE;
    }

    private @NotNull Set<Condition> getConditions(@NotNull ReferenceValue referenceValue, boolean value) {
        return Set.of(new Condition(referenceValue, ConditionType.EQUALITY, Expression.booleanConstant(value)), new Condition(referenceValue, ConditionType.INEQUALITY, Expression.booleanConstant(!value)));
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull BooleanValue getValue() {
        return value;
    }

    public @NotNull Optional<Boolean> getBooleanValue() {
        return value.getBooleanValue();
    }

    @Override
    public @NotNull BooleanConstraint negate() {
        return new BooleanConstraint(getOptionality(), value.negate());
    }

    @Override
    public @NotNull BooleanConstraint and(@NotNull Constraint constraint) {
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException();
        }
        return new BooleanConstraint(getOptionality().combine(constraint.getOptionality()), value.and(booleanCondition.value));
    }

    @Override
    public @NotNull BooleanConstraint or(@NotNull Constraint constraint) {
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException();
        }
        return new BooleanConstraint(getOptionality().combine(constraint.getOptionality()), value.or(booleanCondition.value));
    }

    @Override
    public boolean isFull() {
        return value == BooleanValue.ANY_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return value == BooleanValue.NO_VALUE;
    }

    public enum BooleanValue {
        NO_VALUE,
        ALWAYS_FALSE,
        ANY_VALUE,
        ALWAYS_TRUE;

        public static @NotNull BooleanValue withValue(boolean value) {
            return value ? ALWAYS_TRUE : ALWAYS_FALSE;
        }

        public @NotNull Optional<Boolean> getBooleanValue() {
            return switch (this) {
                case ALWAYS_FALSE -> Optional.of(false);
                case ALWAYS_TRUE -> Optional.of(true);
                default -> Optional.empty();
            };
        }

        public @NotNull BooleanValue negate() {
            return switch (this) {
                case NO_VALUE -> NO_VALUE;
                case ALWAYS_FALSE -> ALWAYS_TRUE;
                case ANY_VALUE -> ANY_VALUE;
                case ALWAYS_TRUE -> ALWAYS_FALSE;
            };
        }

        public @NotNull BooleanValue and(@NotNull BooleanValue value) {
            if (this == NO_VALUE || value == NO_VALUE) {
                return NO_VALUE;
            }
            if (this == ANY_VALUE || value == ANY_VALUE) {
                return ANY_VALUE;
            }
            if (this == BooleanValue.ALWAYS_TRUE) {
                return value == ALWAYS_TRUE ? ALWAYS_TRUE : ALWAYS_FALSE;
            }
            if (this == BooleanValue.ALWAYS_FALSE) {
                return value == ALWAYS_FALSE ? ALWAYS_TRUE : ALWAYS_FALSE;
            }
            throw new AssertionError();
        }

        public @NotNull BooleanValue or(@NotNull BooleanValue value) {
            if (this == NO_VALUE || value == NO_VALUE) {
                return NO_VALUE;
            }
            if (this == ALWAYS_TRUE || value == ALWAYS_TRUE) {
                return ALWAYS_TRUE;
            }
            if (this == ALWAYS_FALSE && value == ALWAYS_FALSE) {
                return ALWAYS_FALSE;
            }
            return ANY_VALUE;
        }
    }
}
