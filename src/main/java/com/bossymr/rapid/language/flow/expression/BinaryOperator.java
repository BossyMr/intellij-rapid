package com.bossymr.rapid.language.flow.expression;

import org.jetbrains.annotations.NotNull;

public enum BinaryOperator {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    INTEGER_DIVIDE("DIV"),
    MODULO("%"),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    EQUAL_TO("="),
    NOT_EQUAL_TO("!="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    AND("AND"),
    XOR("XOR"),
    OR("OR");

    private final @NotNull String text;

    BinaryOperator(@NotNull String text) {
        this.text = text;
    }

    public @NotNull String getText() {
        return text;
    }
}
