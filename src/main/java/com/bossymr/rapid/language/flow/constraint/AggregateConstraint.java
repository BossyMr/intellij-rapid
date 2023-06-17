package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AggregateConstraint(@NotNull RapidType type, @NotNull List<Constraint> values) implements Constraint {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        if (!(constraint instanceof AggregateConstraint aggregateConstraint)) {
            throw new AssertionError();
        }
        if (!(getType().isAssignable(constraint.getType()))) {
            throw new IllegalArgumentException();
        }
        List<Constraint> values = new ArrayList<>();
        for (int i = 0; i < values().size(); i++) {
            values.add(values().get(i).or(aggregateConstraint.values().get(i)));
        }
        return new AggregateConstraint(getType(), values);
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
        if (constraint instanceof AggregateConstraint aggregateConstraint) {
            int size = aggregateConstraint.values().size();
            if (values().size() != size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!(values().get(i).contains(aggregateConstraint.values().get(i)))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
