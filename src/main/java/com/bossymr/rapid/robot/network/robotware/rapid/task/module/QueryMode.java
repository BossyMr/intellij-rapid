package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.network.annotations.Deserializable;

public enum QueryMode {

    @Deserializable("Force")
    FORCE,

    @Deserializable("Try")
    TRY
}
