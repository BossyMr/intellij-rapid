package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum InputOutputSignalType {
    @Alias("DO") DIGITAL_OUTPUT,
    @Alias("DI") DIGITAL_INPUT,
    @Alias("AO") ANALOGUE_OUTPUT,
    @Alias("AI") ANALOGUE_INPUT,
    @Alias("GO") GLOBAL_OUTPUT,
    @Alias("GI") GLOBAL_INPUT,
}
