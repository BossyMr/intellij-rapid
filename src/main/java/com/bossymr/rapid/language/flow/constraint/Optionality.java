package com.bossymr.rapid.language.flow.constraint;

import org.jetbrains.annotations.NotNull;

/**
 * The {@code Optionality} of a variable represents whether it is safe to reference it.
 */
public enum Optionality {
    /**
     * The variable is guaranteed to be present and can be safely referenced.
     */
    PRESENT,

    /**
     * The variable might be missing, and attempting to access the variable might fail.
     */
    UNKNOWN,

    /**
     * The variable is guaranteed to be missing, and attempting to access the variable will always fail.
     */
    MISSING,

    /**
     * The variable is neither missing nor present and the instruction is unreachable.
     * <p>
     * A variable might receive this optionality if one asserts that it is missing after having asserted that it is
     * present.
     */
    NO_VALUE,

    /**
     * The optionality of the variable is irrelevant.
     */
    ANY_VALUE;

    public @NotNull Optionality and(@NotNull Optionality optionality) {
        if (this == ANY_VALUE) {
            return optionality;
        }
        if (optionality == ANY_VALUE) {
            return this;
        }
        if (this == NO_VALUE || optionality == NO_VALUE) {
            return NO_VALUE;
        }
        if (this == UNKNOWN && optionality != UNKNOWN) {
            return optionality;
        }
        if (this != UNKNOWN && optionality == UNKNOWN) {
            return this;
        }
        if (this == PRESENT) {
            return optionality == PRESENT ? PRESENT : NO_VALUE;
        }
        if (this == MISSING) {
            return optionality == MISSING ? MISSING : NO_VALUE;
        }
        return UNKNOWN;
    }

    public @NotNull Optionality or(@NotNull Optionality optionality) {
        if (this == NO_VALUE || this == ANY_VALUE) {
            return optionality;
        }
        if (optionality == NO_VALUE || optionality == ANY_VALUE) {
            return this;
        }
        if (this == UNKNOWN || optionality == UNKNOWN) {
            return UNKNOWN;
        }
        if (this == PRESENT) {
            return optionality == PRESENT ? PRESENT : UNKNOWN;
        }
        if (this == MISSING) {
            return optionality == MISSING ? MISSING : UNKNOWN;
        }
        return UNKNOWN;
    }
}
