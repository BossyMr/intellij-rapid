package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

public enum Constraint {
    ALWAYS_TRUE,
    ALWAYS_FALSE,
    ANY_VALUE,
    NO_VALUE;

    public static @NotNull Constraint of(boolean value) {
        return value ? ALWAYS_TRUE : ALWAYS_FALSE;
    }
}
