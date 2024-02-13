package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.annotations.Deserializable;

public enum ExecutionLevel {

    @Deserializable("Normal")
    NORMAL,

    @Deserializable("Trap")
    TRAP,

    @Deserializable("User")
    USER

}
