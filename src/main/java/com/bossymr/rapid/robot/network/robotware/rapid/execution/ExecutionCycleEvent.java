package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code ExecutionCycleEvent} represents an event with a new execution cycle.
 */
@Entity("rap-ctrlexecstate-ev")
public interface ExecutionCycleEvent {

    /**
     * Returns the current execution cycle.
     *
     * @return the current execution cycle.
     */
    @Property("rapidexeccycle")
    @NotNull ExecutionCycle getState();

}
