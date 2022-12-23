package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum BreakpointState {

    @Deserializable("enabled")
    ENABLED,

    @Deserializable("disabled")
    DISABLED

}
