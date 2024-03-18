package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum TaskExecutionState {

    @Deserializable("ready")
    READY,


    @Deserializable("started")
    STARTED,


    @Deserializable({"stopped", "stop"})
    STOPPED,


    @Deserializable("uninitialized")
    UNINITIALIZED,

}
