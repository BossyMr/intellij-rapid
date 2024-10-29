package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum TaskExecutionState {

    @Alias("ready")
    READY,


    @Alias("started")
    STARTED,


    @Alias({"stopped", "stop"})
    STOPPED,


    @Alias("uninitialized")
    UNINITIALIZED,

}
