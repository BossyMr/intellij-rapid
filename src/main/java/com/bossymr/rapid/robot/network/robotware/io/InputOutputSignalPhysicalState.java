package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum InputOutputSignalPhysicalState {
    @Deserializable("valid") VALID,
    @Deserializable("invalid") INVALID,
}
