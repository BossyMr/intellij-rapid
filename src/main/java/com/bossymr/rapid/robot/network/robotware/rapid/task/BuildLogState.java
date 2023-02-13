package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.annotations.Deserializable;

/**
 * A {@code BuildLogState} represents the result of applying changes to the program.
 */
public enum BuildLogState {

    /**
     * The build failed due to a semantic error.
     */
    @Deserializable("SYS_CTRL_S_RAPID_SEMANTIC_ERROR")
    SEMANTIC_ERROR,

    /**
     * The build failed due to a syntax error.
     */
    @Deserializable("SYS_CTRL_S_RAPID_SYNTAX_ERROR")
    SYNTAX_ERROR,

    /**
     * The build was successful.
     */
    @Deserializable("SYS_CTRL_S_OK")
    OK
}
