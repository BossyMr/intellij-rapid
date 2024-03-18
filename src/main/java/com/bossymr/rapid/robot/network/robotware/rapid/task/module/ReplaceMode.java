package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum ReplaceMode {

    @Deserializable("After")
    AFTER,

    @Deserializable("Before")
    BEFORE,

    @Deserializable("Replace")
    REPLACE
}
