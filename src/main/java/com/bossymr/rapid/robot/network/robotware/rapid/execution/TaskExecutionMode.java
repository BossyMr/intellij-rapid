package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code TaskExecutionMode} is used to determine static or semi-static tasks should be affected.
 */
public enum TaskExecutionMode {

    /**
     * Use {@code Task Panel} state for all tasks.
     */
    @Deserializable("true")
    ALL,

    /**
     * Use {@code Task Panel} state for all normal tasks.
     * <p>
     * If used with
     * {@link ExecutionService#start(RegainMode, ExecutionMode, ExecutionCycle, ConditionState, BreakpointMode,
     * TaskExecutionMode) start(...)}, static and semi-static tasks are always started.
     * <p>
     * If used with {@link ExecutionService#stop(StopMode, TaskExecutionMode) stop(...)}, static and semi-static tasks
     * are never stopped.
     */
    @Deserializable("false")
    NORMAL
}
