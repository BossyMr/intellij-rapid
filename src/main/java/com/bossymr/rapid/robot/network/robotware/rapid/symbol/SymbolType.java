package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

/**
 * {@code SymbolType} represents a type of symbol.
 */
public enum SymbolType {

    @Deserializable("atm")
    ATOMIC,

    @Deserializable("rec")
    RECORD,

    @Deserializable("ali")
    ALIAS,

    @Deserializable("rcp")
    RECORD_COMPONENT,

    @Deserializable("con")
    CONSTANT,

    @Deserializable("var")
    VARIABLE,

    @Deserializable("per")
    PERSISTENT,

    @Deserializable("par")
    PARAMETER,

    @Deserializable("lab")
    LABEL,

    @Deserializable("for")
    FOR_STATEMENT,

    @Deserializable("fun")
    FUNCTION,

    @Deserializable("prc")
    PROCEDURE,

    @Deserializable("trp")
    TRAP,

    @Deserializable("mod")
    MODULE,

    @Deserializable("tsk")
    TASK,

    @Deserializable("any")
    ANY,

    @Deserializable("udef")
    UNDEFINED,
}
