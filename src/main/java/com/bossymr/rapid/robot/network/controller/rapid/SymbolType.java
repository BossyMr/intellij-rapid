package com.bossymr.rapid.robot.network.controller.rapid;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SymbolType {
    @JsonProperty("atm")
    ATOMIC,
    @JsonProperty("rec")
    RECORD,
    @JsonProperty("ali")
    ALIAS,
    @JsonProperty("rcp")
    RECORD_COMPONENT,
    @JsonProperty("con")
    CONSTANT,
    @JsonProperty("var")
    VARIABLE,
    @JsonProperty("per")
    PERSISTENT,
    @JsonProperty("par")
    PARAMETER,
    @JsonProperty("lab")
    LABEL,
    @JsonProperty("for")
    FOR_STATEMENT,
    @JsonProperty("fun")
    FUNCTION,
    @JsonProperty("prc")
    PROCEDURE,
    @JsonProperty("trp")
    TRAP,
    @JsonProperty("mod")
    MODULE,
    @JsonProperty("tsk")
    TASK,
    @JsonProperty("udef")
    UNDEFINED,
    @JsonProperty("any")
    ANY
}
