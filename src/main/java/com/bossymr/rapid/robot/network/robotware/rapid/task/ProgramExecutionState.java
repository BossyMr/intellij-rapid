package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum ProgramExecutionState {

    @Deserializable("ready")
    READY,


    @Deserializable("started")
    STARTED,


    @Deserializable("stopped")
    STOPPED,


    @Deserializable("initiated")
    INITIATED,

}
