package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum TaskType {

    @Deserializable("Normal")
    NORMAL,

    @Deserializable("Static")
    STATIC,

    @Deserializable("SemiStatic")
    SEMI_STATIC

}
