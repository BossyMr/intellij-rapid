package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.network.annotations.Deserializable;

/**
 * An {@code ExecutionMode} is used to determine how to execute the program.
 */
public enum ExecutionMode {

    /**
     * Continous execution.
     */
    @Deserializable("continue")
    CONTINUE,

    /**
     * Executes the next instruction and moves the program pointer to the next instruction. If the next instruction is a
     * routine call, the program pointer is moved to the first instruction of the routine.
     */
    @Deserializable("stepin")
    STEP_IN,

    /**
     * Executes the next instruction and moves the program pointer to the next instruction.
     */
    @Deserializable("stepover")
    STEP_OVER,

    /**
     * Executes remaining instructions in the current routine and moves the program pointer to the next instruction in
     * the calling routine.
     */
    @Deserializable("stepout")
    STEP_OUT,

    /**
     * Executes the previous instruction.
     */
    @Deserializable("stepback")
    STEP_BACK,

    /**
     * Use the same {@code ExecutionMode} as previously.
     */
    @Deserializable("steplast")
    STEP_LAST,

    /**
     * Executes all instructions until the next motion instruction.
     */
    @Deserializable("stepmotion")
    STEP_MOTION
}
