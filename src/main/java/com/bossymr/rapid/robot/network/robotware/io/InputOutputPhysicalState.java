package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.annotations.Deserializable;

public enum InputOutputPhysicalState {
    @Deserializable("halted") HALTED,
    @Deserializable("running") RUNNING,
    @Deserializable("error") ERROR,
    @Deserializable("startup") STARTUP,
    @Deserializable("init") INITIALIZING,
    @Deserializable("unknown") UNKNOWN,
}
