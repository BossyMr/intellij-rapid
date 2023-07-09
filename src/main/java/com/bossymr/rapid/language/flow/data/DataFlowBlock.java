package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A {@code DataFlowBlock} represents the state of a program at the end of a {@link BasicBlock}. A block contains a
 * number of mutually exlusive states, see to the documentation of {@link DataFlowState}.
 *
 * @param basicBlock the block.
 * @param predecessors the predecessors to this block.
 * @param successors the successors to this block.
 * @param states the states of this block.
 */
public record DataFlowBlock(@NotNull BasicBlock basicBlock,
                            @NotNull Set<DataFlowEdge> predecessors,
                            @NotNull Set<DataFlowEdge> successors,
                            @NotNull Set<DataFlowState> states) {

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
        for (Condition result : condition.solve()) {
            for (DataFlowState state : states) {
               state.setCondition(result);
            }
        }
    }
}
