package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record UnionConstraint(@NotNull RapidType type, @NotNull List<Constraint> constraints) implements Constraint {

    public UnionConstraint {
        if (constraints.size() < 2) {
            throw new IllegalArgumentException("Cannot create union of: " + constraints);
        }
        for (int i = 1; i < constraints.size(); i++) {
            Constraint constraint = constraints.get(i);
            if (!(type.isAssignable(constraint.type()))) {
                throw new IllegalArgumentException("Constraint: " + constraint + " is not assignable to: " + type());
            }
        }
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (!(type.isAssignable(constraint.type()))) {
            throw new IllegalArgumentException("Constraint: " + constraint + " is not assignable to: " + type());
        }
        constraints.add(constraint);
        return this;
    }

    @Override
    public @NotNull Constraint not() {
        return Constraint.and(constraints().stream()
                .map(Constraint::not)
                .toList());
    }

    @Override
    public boolean contains(@NotNull Constraint constraint) {
        return constraints().stream().anyMatch(value -> value.contains(constraint));
    }
}
