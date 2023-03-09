package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.annotations.Deserializable;

public enum TaskType {

    @Deserializable("Normal")
    NORMAL,

    @Deserializable("Static")
    STATIC,

    @Deserializable("SemiStatic")
    SEMI_STATIC

}
