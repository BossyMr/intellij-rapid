package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * A {@code ConditionState} is used to determine what conditions must be fulfilled prior to starting execution.
 */
public enum ConditionState {

    /**
     * No check is performed.
     */
    @Alias("none")
    NONE,

    /**
     * Check that the task entry point is the call chain root.
     */
    @Alias("callchain")
    CALLCHAIN
}
