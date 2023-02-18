package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;

public enum EventLogMessageType {

    @Deserializable("1")
    INFORMATION,

    @Deserializable("2")
    WARNING,

    @Deserializable("3")
    ERROR
}
