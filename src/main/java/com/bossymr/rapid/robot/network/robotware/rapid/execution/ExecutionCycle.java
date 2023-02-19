package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code ExecutionCycle} represents the execution mode.
 */
public enum ExecutionCycle {

    /**
     * The previous execution cycle counter should be kept.
     */
    @Deserializable("asis")
    AS_IS,

    /**
     * Execution should loop indefinitely.
     */
    @Deserializable("forever")
    FOREVER,

    /**
     * Execution should execute once.
     */
    @Deserializable("once")
    ONCE,

    @Deserializable("oncedone")
    ONCE_DONE

}
