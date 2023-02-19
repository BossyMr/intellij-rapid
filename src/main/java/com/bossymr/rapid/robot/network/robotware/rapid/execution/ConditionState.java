package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code ConditionState} is used to determine what conditions must be fulfilled prior to starting execution.
 */
public enum ConditionState {

    /**
     * No check is performed.
     */
    @Deserializable("none")
    NONE,

    /**
     * Check that the task entry point is the call chain root.
     */
    @Deserializable("callchain")
    CALLCHAIN
}
