package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum TaskActiveState {

    @Deserializable("On")
    ENABLED,

    @Deserializable("Off")
    DISABLED

}
