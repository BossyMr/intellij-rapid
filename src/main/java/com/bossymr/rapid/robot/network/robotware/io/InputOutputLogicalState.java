package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum InputOutputLogicalState {
    @Deserializable("started") STARTED,
    @Deserializable("stopped") STOPPED,
    @Deserializable("unknown") UNKNOWN,
}
