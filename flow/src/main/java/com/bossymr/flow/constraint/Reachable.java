package com.bossymr.flow.constraint;

/**
 * Indicates whether an instruction is reachable.
 */
public enum Reachable {
    /**
     * The instruction is reachable.
     */
    REACHABLE,

    /**
     * The instruction is not reachable.
     */
    NOT_REACHABLE,

    /**
     * It isn't possible to determine whether this instruction is reachable.
     */
    UNKNOWN
}
