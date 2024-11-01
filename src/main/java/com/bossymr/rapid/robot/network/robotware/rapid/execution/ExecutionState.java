package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * A {@code ExecutionState} represents the execution state.
 */
public enum ExecutionState {

    /**
     * The task is currently executing or performing {@link RegainMode regain}.
     */
    @Alias("running")
    RUNNING,

    /**
     * The task is not currently executing or performing {@link RegainMode regain}.
     */
    @Alias("stopped")
    STOPPED

}
