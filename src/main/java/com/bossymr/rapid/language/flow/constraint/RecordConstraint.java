package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record RecordConstraint(@NotNull RapidType type, @NotNull Map<String, Constraint> components) implements Constraint {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (!(constraint instanceof RecordConstraint recordConstraint)) {
            throw new AssertionError();
        }
        if (!(getType().isAssignable(constraint.getType()))) {
            throw new IllegalArgumentException();
        }
        Map<String, Constraint> components = new HashMap<>();
        components().forEach((name, value) -> {
            components.put(name, value.or(recordConstraint.components().get(name)));
        });
        return new RecordConstraint(getType(), components);
    }

    @Override
    public @NotNull Constraint not() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        if (!(getType().isAssignable(constraint.getType()))) {
            throw new IllegalArgumentException();
        }
        if (constraint instanceof RecordConstraint recordConstraint) {
            return recordConstraint.components().entrySet().stream()
                    .allMatch(entry -> components().containsKey(entry.getKey()) && components().get(entry.getKey()).contains(entry.getValue()));
        }
        return false;
    }
}
