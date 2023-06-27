package com.bossymr.rapid.language.flow.constraint;

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
    MISSING
}
