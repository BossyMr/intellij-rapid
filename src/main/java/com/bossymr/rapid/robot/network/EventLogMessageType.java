package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum EventLogMessageType {

    @Alias("1")
    INFORMATION,

    @Alias("2")
    WARNING,

    @Alias("3")
    ERROR
}
