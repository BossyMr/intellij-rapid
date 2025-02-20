package com.bossymr.flow.constraint;

/**
 * Indicates the possible values of a boolean expression.
 */
public enum Constraint {
    /**
     * The expression can be equal to either {@code true} or {@code false}.
     */
    ANY_VALUE,

    /**
     * The expression will only ever be equal to {@code false}.
     */
    ALWAYS_FALSE,

    /**
     * The expression will only ever be equal to {@code true}.
     */
    ALWAYS_TRUE,

    /**
     * The expression is not reachable.
     */
    NO_VALUE,

    /**
     * It isn't possible to determine the possible values of the expression.
     */
    UNKNOWN,
}
