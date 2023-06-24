package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.NotNull;

public record TopConstraint(@NotNull Optionality optionality) implements Condition {

    @Override
    public @NotNull Condition copy(@NotNull Optionality optionality) {
        return new TopConstraint(optionality);
    }

    @Override
    public @NotNull Condition negate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Condition and(@NotNull Condition condition) {
        return condition;
    }

    @Override
    public @NotNull Condition or(@NotNull Condition condition) {
        return this;
    }
}
