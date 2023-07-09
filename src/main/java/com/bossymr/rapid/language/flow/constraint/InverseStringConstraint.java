package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record InverseStringConstraint(@NotNull Optionality optionality, @NotNull Set<String> sequences) implements Constraint {

    @Override
    public @NotNull Set<Set<Condition>> toConditions(@NotNull ReferenceValue referenceValue) {
        Set<Condition> conditions = new HashSet<>();
        for (String sequence : sequences) {
            conditions.add(new Condition(referenceValue, ConditionType.INEQUALITY, Expression.stringConstant(sequence)));
        }
        return Set.of(conditions);
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality();
    }

    @Override
    public @NotNull StringConstraint negate() {
        return new StringConstraint(getOptionality(), sequences());
    }

    @Override
    public @NotNull Constraint and(@NotNull Constraint constraint) {
        return constraint.and(this);
    }

    @Override
    public @NotNull Constraint or(@NotNull Constraint constraint) {
        return constraint.or(this);
    }

    @Override
    public boolean isFull() {
        return sequences.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
