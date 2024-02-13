package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;

/**
 * A {@code HoldToRunMode} is used to allow execution while the robot is in manual mode.
 *
 * @see ExecutionService#setHoldToRun(HoldToRunMode)
 */
public enum HoldToRunMode {

    @Deserializable("press")
    PRESS,

    @Deserializable("held")
    HELD,
    @Deserializable("release")
    RELEASE,
}
