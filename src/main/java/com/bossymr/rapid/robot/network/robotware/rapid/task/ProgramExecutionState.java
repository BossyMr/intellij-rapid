package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum ProgramExecutionState {

    @Alias("ready")
    READY,


    @Alias("started")
    STARTED,


    @Alias("stopped")
    STOPPED,


    @Alias("initiated")
    INITIATED,

}
