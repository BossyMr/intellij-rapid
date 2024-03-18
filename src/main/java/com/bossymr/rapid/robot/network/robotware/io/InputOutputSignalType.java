package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum InputOutputSignalType {
    @Deserializable("DO") DIGITAL_OUTPUT,
    @Deserializable("DI") DIGITAL_INPUT,
    @Deserializable("AO") ANALOGUE_OUTPUT,
    @Deserializable("AI") ANALOGUE_INPUT,
    @Deserializable("GO") GLOBAL_OUTPUT,
    @Deserializable("GI") GLOBAL_INPUT,
}
