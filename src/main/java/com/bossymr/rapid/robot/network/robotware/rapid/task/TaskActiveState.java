package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.annotations.Deserializable;

public enum TaskActiveState {

    @Deserializable("On")
    ENABLED,

    @Deserializable("Off")
    DISABLED

}
