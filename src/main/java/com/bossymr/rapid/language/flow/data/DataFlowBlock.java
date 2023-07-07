package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                            @NotNull Set<DataFlowBlock> predecessors,
                            @NotNull Set<DataFlowBlock> successors,
                            @NotNull List<DataFlowState> states) {

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
        List<Condition> solve = condition.solve();
        for (Condition result : solve) {
            for (DataFlowState state : states) {
               state.setCondition(result);
            }
        }
    }

    public @NotNull List<DataFlowState> split(@NotNull Function<DataFlowState, List<DataFlowState>> consumer) {
        return states.stream()
                .flatMap(state -> consumer.apply(state).stream())
                .toList();
    }

    public @NotNull List<DataFlowState> getStates(@NotNull Condition condition) {
        return states.stream()
                .filter(state -> state.intersects(condition))
                .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                .collect(Collectors.toList());
    }
}
