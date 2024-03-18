package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum InputOutputSignalLogicalState {
    @Deserializable("simulated") SIMULATED,
    @Deserializable("not simulated") NOT_SIMULATED
}
