package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.rapid.robot.network.HoldToRunState;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code HoldToRunEvent} represents an event with a new hold to run state.
 */
@Entity("rap-hdtr-ev")
public interface HoldToRunEvent {

    /**
     * Returns the current hold to run state.
     *
     * @return the current hold to run state.
     */
    @Property("hdtr-State")
    @NotNull HoldToRunState getState();

}
