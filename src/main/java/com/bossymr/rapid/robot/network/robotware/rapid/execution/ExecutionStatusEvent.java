package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code ExecutionStatusEvent} represents an event with a new execution state.
 */
@Entity("rap-ctrlexecstate-ev")
public interface ExecutionStatusEvent extends EntityModel {

    /**
     * Returns the current execution state.
     *
     * @return the current execution state.
     */
    @Property("ctrlexecstate")
    @NotNull ExecutionState getState();

}
