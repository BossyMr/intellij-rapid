package com.bossymr.rapid.network.controller.io;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PhysicalState {
    @JsonProperty("halted")
    HALTED,
    @JsonProperty("running")
    RUNNING,
    @JsonProperty("error")
    ERROR,
    @JsonProperty("startup")
    STARTUP,
    @JsonProperty("init")
    INITIALIZED,
    @JsonProperty("unknown")
    UNKNOWN
}
