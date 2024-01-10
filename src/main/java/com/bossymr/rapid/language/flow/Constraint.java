package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

public enum Constraint {
    NO_VALUE,
    ALWAYS_FALSE,
    ANY_VALUE,
    ALWAYS_TRUE;

    public @NotNull Constraint or(@NotNull Constraint value) {
        if (this == value || value == NO_VALUE) {
            return this;
        }
        if (this == NO_VALUE) {
            return value;
        }
        return ANY_VALUE;
    }
}
