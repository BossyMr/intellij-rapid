package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum InputOutputPhysicalState {
    @Alias("halted") HALTED,
    @Alias("running") RUNNING,
    @Alias("error") ERROR,
    @Alias("startup") STARTUP,
    @Alias("init") INITIALIZING,
    @Alias("unknown") UNKNOWN,
}
