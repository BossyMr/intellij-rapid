package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * A {@code BreakpointMode} is used to determine whether to stop at encountered breakpoints.
 */
public enum BreakpointMode {

    /**
     * Execution is stopped at encountered breakpoints.
     */
    @Alias("enabled")
    ENABLED,

    /**
     * Execution is not stopped at encountered breakpoints.
     */
    @Alias("disabled")
    DISABLED

}
