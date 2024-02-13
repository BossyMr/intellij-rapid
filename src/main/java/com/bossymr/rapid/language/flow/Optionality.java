package com.bossymr.rapid.language.flow;

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
    NO_VALUE;

    public @NotNull Optionality or(@NotNull Optionality optionality) {
        if (this == optionality || optionality == NO_VALUE) {
            return this;
        }
        if (this == NO_VALUE) {
            return optionality;
        }
        return UNKNOWN;
    }
}
