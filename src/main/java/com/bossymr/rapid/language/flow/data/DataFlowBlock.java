package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A {@code DataFlowBlock} represents the state of a program at the end of a {@link BasicBlock}. A block contains a
 * number of mutually exlusive states, see to the documentation of {@link DataFlowState}.
 */
public final class DataFlowBlock {

    private final @NotNull BasicBlock basicBlock;
    private final @NotNull Set<DataFlowState> states;

    private final @NotNull Set<DataFlowEdge> predecessors;
    private final @NotNull Set<DataFlowEdge> successors;

    public DataFlowBlock(@NotNull BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
        this.predecessors = new HashSet<>(1);
        this.successors = new HashSet<>(1);
        this.states = new HashSet<>(1);
    }

    public @NotNull Constraint getConstraint(@NotNull Expression expression) {
        return Constraint.or(states.stream()
                .map(state -> state.getConstraint(expression))
                .toList());
    }

    public @NotNull Constraint getConstraint(@NotNull Value value) {
        return Constraint.or(states.stream()
                .map(state -> state.getConstraint(value))
                .toList());
    }

    public void add(@NotNull Condition condition) {
        for (Condition result : condition.getVariants()) {
            for (DataFlowState state : states) {
                state.add(result);
            }
        }
    }

    public @NotNull BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public @NotNull Set<DataFlowEdge> getPredecessors() {
        return predecessors;
    }

    public @NotNull Set<DataFlowEdge> getSuccessors() {
        return successors;
    }

    public @NotNull Set<DataFlowState> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlowBlock that = (DataFlowBlock) o;
        return Objects.equals(basicBlock, that.basicBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicBlock);
    }

    @Override
    public String toString() {
        return "DataFlowBlock{" +
                "basicBlock=" + basicBlock +
                ", states=" + states +
                '}';
    }
}
