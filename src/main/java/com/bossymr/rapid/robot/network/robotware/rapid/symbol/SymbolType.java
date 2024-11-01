package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * {@code SymbolType} represents a type of symbol.
 */
public enum SymbolType {

    @Alias("atm")
    ATOMIC,

    @Alias("rec")
    RECORD,

    @Alias("ali")
    ALIAS,

    @Alias("rcp")
    RECORD_COMPONENT,

    @Alias("con")
    CONSTANT,

    @Alias("var")
    VARIABLE,

    @Alias("per")
    PERSISTENT,

    @Alias("par")
    PARAMETER,

    @Alias("lab")
    LABEL,

    @Alias("for")
    FOR_STATEMENT,

    @Alias("fun")
    FUNCTION,

    @Alias("prc")
    PROCEDURE,

    @Alias("trp")
    TRAP,

    @Alias("mod")
    MODULE,

    @Alias("tsk")
    TASK,

    @Alias("any")
    ANY,

    @Alias("udef")
    UNDEFINED,
}
