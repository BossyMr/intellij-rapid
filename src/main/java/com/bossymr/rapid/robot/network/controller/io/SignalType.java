package com.bossymr.rapid.robot.network.controller.io;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SignalType {
    @JsonProperty("DO")
    DIGITAL_OUTPUT,
    @JsonProperty("DI")
    DIGITAL_INPUT,
    @JsonProperty("AO")
    ANALOGUE_OUTPUT,
    @JsonProperty("AI")
    ANALOGUE_INPUT,
    @JsonProperty("GI")
    GLOBAL_INPUT,
    @JsonProperty("GO")
    GLOBAL_OUTPUT
}
