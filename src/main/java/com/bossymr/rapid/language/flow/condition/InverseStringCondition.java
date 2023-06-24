package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record InverseStringCondition(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Condition {

    @Override
    public @NotNull InverseStringCondition copy(@NotNull Optionality optionality) {
        return new InverseStringCondition(optionality, sequences());
    }

    @Override
    public @NotNull StringCondition negate() {
        return new StringCondition(optionality(), sequences());
    }

    @Override
    public @NotNull Condition and(@NotNull Condition condition) {
        return condition.and(this);
    }

    @Override
    public @NotNull Condition or(@NotNull Condition condition) {
        return condition.or(this);
    }
}
