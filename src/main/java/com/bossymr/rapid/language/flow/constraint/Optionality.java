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
    MISSING;

    public @NotNull Optionality combine(@NotNull Optionality optionality) {
        if (this == MISSING && optionality == MISSING) {
            return Optionality.MISSING;
        }
        if (this == MISSING || optionality == MISSING || this == UNKNOWN || optionality == UNKNOWN) {
            return Optionality.UNKNOWN;
        }
        return Optionality.PRESENT;
    }
}
