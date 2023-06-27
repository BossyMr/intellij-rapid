package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanConstraint implements Constraint {

    private final @NotNull Optionality optionality;
    private final @NotNull BooleanValue value;

    public BooleanConstraint(@NotNull Optionality optionality, @NotNull BooleanValue value) {
        this.optionality = optionality;
        this.value = value;
    }

    @Override
    public @NotNull Optionality optionality() {
        return optionality;
    }


    public @NotNull BooleanValue getValue() {
        return value;
    }

    @Override
    public @NotNull BooleanConstraint copy(@NotNull Optionality optionality) {
        return new BooleanConstraint(optionality(), value);
    }

    @Override
    public @NotNull BooleanConstraint negate() {
        return new BooleanConstraint(optionality(), value.negate());
    }

    @Override
    public @NotNull BooleanConstraint and(@NotNull Constraint constraint) {
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException();
        }
        return new BooleanConstraint(optionality(), value.and(booleanCondition.value));
    }

    @Override
    public @NotNull BooleanConstraint or(@NotNull Constraint constraint) {
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException();
        }
        return new BooleanConstraint(optionality(), value.or(booleanCondition.value));
    }

    public enum BooleanValue {
        NO_VALUE,
        ALWAYS_FALSE,
        ANY_VALUE,
        ALWAYS_TRUE;

        public static @NotNull BooleanValue get(boolean value) {
            return value ? ALWAYS_TRUE : ALWAYS_FALSE;
        }

        public @Nullable Boolean get() {
            return switch (this) {
                case NO_VALUE, ANY_VALUE -> null;
                case ALWAYS_FALSE -> false;
                case ALWAYS_TRUE -> true;
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
            throw new IllegalStateException("Unexpected value: " + this);
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
