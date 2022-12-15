package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ExecutionState {

    @Deserializable("running")
    RUNNING,

    @Deserializable("stopped")
    STOPPED

}
