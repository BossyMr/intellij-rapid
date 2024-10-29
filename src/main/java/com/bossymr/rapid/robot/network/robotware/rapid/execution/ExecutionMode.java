package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * An {@code ExecutionMode} is used to determine how to execute the program.
 */
public enum ExecutionMode {

    /**
     * Continous execution.
     */
    @Alias("continue")
    CONTINUE,

    /**
     * Executes the next instruction and moves the program pointer to the next instruction. If the next instruction is a
     * routine call, the program pointer is moved to the first instruction of the routine.
     */
    @Alias("stepin")
    STEP_IN,

    /**
     * Executes the next instruction and moves the program pointer to the next instruction.
     */
    @Alias("stepover")
    STEP_OVER,

    /**
     * Executes remaining instructions in the current routine and moves the program pointer to the next instruction in
     * the calling routine.
     */
    @Alias("stepout")
    STEP_OUT,

    /**
     * Executes the previous instruction.
     */
    @Alias("stepback")
    STEP_BACK,

    /**
     * Use the same {@code ExecutionMode} as previously.
     */
    @Alias("steplast")
    STEP_LAST,

    /**
     * Executes all instructions until the next motion instruction.
     */
    @Alias("stepmotion")
    STEP_MOTION
}
