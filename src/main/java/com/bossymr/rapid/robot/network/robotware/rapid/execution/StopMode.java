package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code StopMode} determines how to stop the program.
 */
public enum StopMode {

    /**
     * Stops execution when the current cycle is completed.
     */
    @Deserializable("cycle")
    CYCLE,

    /**
     * Stops execution when the current instruction is completed.
     */
    @Deserializable("instr")
    INSTRUCTION,

    /**
     * Stops execution immediately.
     */
    @Deserializable("stop")
    STOP,

    /**
     * Quickly stops execution immediately.
     */
    @Deserializable("qstop")
    QUICK_STOP
}
