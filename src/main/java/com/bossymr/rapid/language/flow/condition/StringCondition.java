package com.bossymr.rapid.language.flow.condition;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record StringCondition(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Condition {

    @Override
    public @NotNull StringCondition copy(@NotNull Optionality optionality) {
        return new StringCondition(optionality, sequences());
    }

    @Override
    public @NotNull InverseStringCondition negate() {
        return new InverseStringCondition(optionality(), sequences());
    }

    @Override
    public @NotNull Condition and(@NotNull Condition condition) {
        if (condition instanceof InverseStringCondition inverseStringCondition) {
            StringCondition copy = copy(optionality());
            copy.sequences().removeAll(inverseStringCondition.sequences());
            return copy;
        }
        if (condition instanceof StringCondition stringCondition) {
            StringCondition copy = stringCondition.copy(stringCondition.optionality());
            copy.sequences().retainAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public @NotNull Condition or(@NotNull Condition condition) {
        if (condition instanceof InverseStringCondition inverseStringCondition) {
            InverseStringCondition copy = inverseStringCondition.copy(inverseStringCondition.optionality());
            copy.sequences().removeAll(sequences());
            return copy;
        }
        if (condition instanceof StringCondition stringCondition) {
            StringCondition copy = stringCondition.copy(stringCondition.optionality());
            copy.sequences().addAll(sequences());
            return copy;
        }
        throw new IllegalArgumentException();
    }
}
