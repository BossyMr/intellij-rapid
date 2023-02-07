package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.annotations.Deserializable;

public enum InputOutputSignalPhysicalState {
    @Deserializable("valid") VALID,
    @Deserializable("invalid") INVALID,
}
