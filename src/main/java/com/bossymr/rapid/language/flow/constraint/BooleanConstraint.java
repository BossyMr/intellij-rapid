package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class BooleanConstraint implements Constraint {

    private static final @NotNull BooleanConstraint ANY_VALUE = new BooleanConstraint(BooleanValue.ANY_VALUE);

    private static final @NotNull BooleanConstraint ALWAYS_TRUE = new BooleanConstraint(BooleanValue.ALWAYS_TRUE);

    private static final @NotNull BooleanConstraint ALWAYS_FALSE = new BooleanConstraint(BooleanValue.ALWAYS_FALSE);

    private static final @NotNull BooleanConstraint NO_VALUE = new BooleanConstraint(BooleanValue.NO_VALUE);

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

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    @Override
    public @NotNull Constraint setOptionality(@NotNull Optionality optionality) {
        return new BooleanConstraint(optionality, value);
    }

    public @NotNull Optional<Boolean> getValue() {
        return switch (value) {
            case NO_VALUE, ANY_VALUE -> Optional.empty();
            case ALWAYS_FALSE -> Optional.of(false);
            case ALWAYS_TRUE -> Optional.of(true);
        };
    }

    public @NotNull Optional<Boolean> getBooleanValue() {
        return value.getBooleanValue();
    }

    @Override
    public @NotNull BooleanConstraint negate() {
        return new BooleanConstraint(getOptionality(), value.negate());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.and(this);
        }
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException("Cannot create intersection of: " + this + " and " + constraint);
        }
        return new BooleanConstraint(getOptionality().and(constraint.getOptionality()), value.and(booleanCondition.value));
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (constraint instanceof OpenConstraint || constraint instanceof ClosedConstraint) {
            return constraint.or(this);
        }
        if (!(constraint instanceof BooleanConstraint booleanCondition)) {
            throw new IllegalArgumentException("Cannot create union of: " + this + " and " + constraint);
        }
        return new BooleanConstraint(getOptionality().or(constraint.getOptionality()), value.or(booleanCondition.value));
    }

    @Override
    public boolean isFull() {
        return value == BooleanValue.ANY_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return value == BooleanValue.NO_VALUE;
    }

    @Override
    public @NotNull String getPresentableText() {
        return switch (value) {
            case NO_VALUE -> "[]";
            case ALWAYS_FALSE -> "[false]";
            case ANY_VALUE -> "[true, false]";
            case ALWAYS_TRUE -> "[true]";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanConstraint that = (BooleanConstraint) o;
        return optionality == that.optionality && value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionality, value);
    }

    @Override
    public String toString() {
        return "BooleanConstraint{" +
                "optionality=" + optionality +
                ", value=" + value +
                '}';
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
            if (this == ANY_VALUE && value != ANY_VALUE) {
                return value;
            }
            if (this != ANY_VALUE && value == ANY_VALUE) {
                return this;
            }
            if (this == ALWAYS_TRUE) {
                return value == ALWAYS_TRUE ? ALWAYS_TRUE : NO_VALUE;
            }
            if (this == ALWAYS_FALSE) {
                return value == ALWAYS_FALSE ? ALWAYS_FALSE : NO_VALUE;
            }
            return ANY_VALUE;
        }

        public @NotNull BooleanValue or(@NotNull BooleanValue value) {
            if (this == NO_VALUE) {
                return value;
            }
            if (value == NO_VALUE) {
                return this;
            }
            if (this == ANY_VALUE || value == ANY_VALUE) {
                return ANY_VALUE;
            }
            if (this == ALWAYS_TRUE) {
                return value == ALWAYS_TRUE ? ALWAYS_TRUE : ANY_VALUE;
            }
            if (this == ALWAYS_FALSE) {
                return value == ALWAYS_FALSE ? ALWAYS_FALSE : ANY_VALUE;
            }
            return ANY_VALUE;
        }
    }
}
