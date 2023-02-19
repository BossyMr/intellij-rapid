package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code RegainMode} is used to determine how to move to the next target, if the robot started execution in the
 * middle of the program, and is not on the path between the previous, and next target.
 */
public enum RegainMode {

    /**
     * Move the robot back to the path if it is within the configurable maximum distance from the path.
     */
    @Deserializable("continue")
    CONTINUE,

    /**
     * Move the robot back to the path.
     */
    @Deserializable("regain")
    REGAIN,

    /**
     * Clear the path and continue movement directly to the next target.
     */
    @Deserializable("clear")
    CLEAR

}
