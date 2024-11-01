package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * A {@code StopMode} determines how to stop the program.
 */
public enum StopMode {

    /**
     * Stops execution when the current cycle is completed.
     */
    @Alias("cycle")
    CYCLE,

    /**
     * Stops execution when the current instruction is completed.
     */
    @Alias("instr")
    INSTRUCTION,

    /**
     * Stops execution immediately.
     */
    @Alias("stop")
    STOP,

    /**
     * Quickly stops execution immediately.
     */
    @Alias("qstop")
    QUICK_STOP
}
