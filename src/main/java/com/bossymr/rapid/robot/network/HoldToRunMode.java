package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Alias;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;

/**
 * A {@code HoldToRunMode} is used to allow execution while the robot is in manual mode.
 *
 * @see ExecutionService#setHoldToRun(HoldToRunMode)
 */
public enum HoldToRunMode {

    @Alias("press")
    PRESS,

    @Alias("held")
    HELD,
    @Alias("release")
    RELEASE,
}
