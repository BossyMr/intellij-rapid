package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum ExecutionLevel {

    @Deserializable("Normal")
    NORMAL,

    @Deserializable("Trap")
    TRAP,

    @Deserializable("User")
    USER

}
