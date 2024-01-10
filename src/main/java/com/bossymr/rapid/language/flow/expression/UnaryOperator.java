package com.bossymr.rapid.language.flow.expression;

import org.jetbrains.annotations.NotNull;

/**
 * A {@code UnaryOperator} represents an operation which can be performed in a {@link UnaryExpression}.
 */
public enum UnaryOperator {
    NOT("!"), NEGATE("-"), PRESENT("present"), DIMENSION("dim");

    private final @NotNull String text;

    UnaryOperator(@NotNull String text) {
        this.text = text;
    }

    public @NotNull String getText() {
        return text;
    }
}
