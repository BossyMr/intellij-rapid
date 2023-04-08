package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code ExecutionStatus} represents the current execution state.
 */
@Entity("rap-execution")
public interface ExecutionStatus {

    /**
     * Returns the current execution state.
     *
     * @return the current execution state.
     */
    @Property("ctrlexecstate")
    @NotNull ExecutionState getState();

    /**
     * Returns the current execution mode.
     *
     * @return the current execution mode.
     */
    @Property("cycle")
    @NotNull ExecutionCycle getCycle();

}
