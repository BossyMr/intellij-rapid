package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.annotations.Deserializable;

public enum TaskExecutionState {

    @Deserializable("ready")
    READY,


    @Deserializable("started")
    STARTED,


    @Deserializable("stop")
    STOPPED,


    @Deserializable("uninitialized")
    UNINITIALIZED,

}
