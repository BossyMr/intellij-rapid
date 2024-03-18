package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum QueryMode {

    @Deserializable("Force")
    FORCE,

    @Deserializable("Try")
    TRY
}
