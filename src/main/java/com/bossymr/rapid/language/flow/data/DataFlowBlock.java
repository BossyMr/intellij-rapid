package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.Value;
import com.bossymr.rapid.language.flow.value.VariableSnapshot;
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

    public static @NotNull Constraint getConstraint(@NotNull Set<DataFlowState> states, @NotNull Value value) {
        return Constraint.or(states.stream()
                .map(state -> state.getConstraint(value))
                .toList());
    }

    public @NotNull Constraint getConstraint(@NotNull Value value) {
        if (states.isEmpty()) {
            return Constraint.any(value.getType());
        }
        return getConstraint(states, value);
    }

    public @NotNull Constraint getHistoricConstraint(@NotNull ReferenceValue value, @NotNull LinearInstruction.AssignmentInstruction instruction) {
        if (states.isEmpty()) {
            return Constraint.any(value.getType());
        }
        // TODO: 2023-07-21 This returns NO_VALUE
        return Constraint.or(states.stream()
                .map(state -> state.getConditions(instruction.variable()).stream()
                        .filter(condition -> condition.getExpression().getClass().equals(instruction.value().getClass()))
                        .flatMap(condition -> condition.getVariables().stream())
                        .filter(variable -> variable instanceof VariableSnapshot)
                        .map(variable -> (VariableSnapshot) variable)
                        .filter(snapshot -> snapshot.getReferenceValue().isPresent())
                        .filter(snapshot -> snapshot.getReferenceValue().orElseThrow().equals(value))
                        .map(state::getConstraint)
                        .toList())
                .map(constraints -> {
                    if (constraints.isEmpty()) {
                        return Constraint.any(value.getType());
                    } else {
                        return Constraint.or(constraints);
                    }
                })
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
