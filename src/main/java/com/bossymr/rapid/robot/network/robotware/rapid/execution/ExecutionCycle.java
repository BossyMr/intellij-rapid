package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * A {@code ExecutionCycle} represents the execution mode.
 */
public enum ExecutionCycle {

    /**
     * The previous execution cycle counter should be kept.
     */
    @Alias("asis")
    AS_IS,

    /**
     * Execution should loop indefinitely.
     */
    @Alias("forever")
    FOREVER,

    /**
     * Execution should execute once.
     */
    @Alias("once")
    ONCE,

    @Alias("oncedone")
    ONCE_DONE

}
