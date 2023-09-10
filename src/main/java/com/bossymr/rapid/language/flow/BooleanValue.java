package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum BooleanValue {
    NO_VALUE,
    ALWAYS_FALSE,
    ANY_VALUE,
    ALWAYS_TRUE;

    public static @NotNull BooleanValue of(boolean value) {
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
