package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataFlowBlock {

    private final @NotNull Instruction instruction;
    private final @NotNull List<DataFlowState> states = new ArrayList<>();

    private final @NotNull Set<DataFlowEdge> successors = new HashSet<>();
    private final @NotNull Set<DataFlowEdge> predecessors = new HashSet<>();

    public DataFlowBlock(@NotNull Instruction instruction) {
        this.instruction = instruction;
    }

    public static @Nullable DataFlowState getPreviousCycle(@NotNull DataFlowState state) {
        DataFlowBlock dataFlowBlock = state.getBlock();
        if (dataFlowBlock == null) {
            return null;
        }
        DataFlowState predecessor = state;
        while ((predecessor = predecessor.getPredecessor()) != null) {
            if (predecessor.getBlock() == null) {
                continue;
            }
            if (predecessor.getBlock().getInstruction().equals(dataFlowBlock.getInstruction())) {
                return predecessor;
            }
        }
        return null;
    }

    public static int getPreviousCycles(@NotNull DataFlowState state) {
        int count = 0;
        while ((state = getPreviousCycle(state)) != null) {
            count++;
        }
        return count;
    }

    public @NotNull Instruction getInstruction() {
        return instruction;
    }

    public @NotNull List<DataFlowState> getStates() {
        return states;
    }

    public @NotNull Set<DataFlowEdge> getSuccessors() {
        return successors;
    }

    public @NotNull Set<DataFlowEdge> getPredecessors() {
        return predecessors;
    }

    public void assign(@NotNull DataFlowState state, @NotNull ReferenceExpression variable, @NotNull Expression expression) {
        if (!(variable.getType().isAssignable(expression.getType()))) {
            throw new IllegalArgumentException("Cannot assign expression: " + expression + " (type: " + expression.getType() + ") to variable of type: " + variable.getType());
        }
        DataFlowState previousCycle = getPreviousCycle(state);
        if (previousCycle != null) {
            /*
             * If this block is in a loop (i.e. a state with this instruction is a predecessor to this state) and
             * this assignment is cyclic in nature: this assignment has the previous snapshot (or a snapshot
             * which is newer than the root snapshot of the previous iteration) of the variable which is being
             * assigned in the expression. For example, x2 := x1 + 1 or x3 := x1 + 1 (if x1 is the latest snapshot
             * for x at that point).
             */
            state.assign(variable, null);
        } else {
            state.assign(variable, expression);
        }
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression variable) {
        Optionality optionality = null;
        for (DataFlowState state : states) {
            SnapshotExpression snapshot = state.getRoot(variable);
            ReferenceExpression expression = snapshot != null ? snapshot : variable;
            if (optionality == null) {
                optionality = state.getOptionality(expression);
            } else {
                Optionality result = state.getOptionality(expression);
                optionality = optionality.or(result);
            }
        }
        if (optionality == null) {
            throw new IllegalStateException("Malformed DataFlowBlock: " + this);
        }
        return optionality;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        BooleanValue booleanValue = BooleanValue.NO_VALUE;
        for (DataFlowState state : states) {
            booleanValue = booleanValue.or(state.getConstraint(expression));
        }
        return booleanValue;
    }

    public void addSuccessor(@NotNull DataFlowBlock successor, @NotNull DataFlowState state) {
        DataFlowBlock block = state.getBlock();
        if (block != null) {
            if (block != successor) {
                state = DataFlowState.createSuccessorState(successor, state);
            }
        }
        if (getPreviousCycles(state) >= 2) {
            return;
        }
        DataFlowEdge edge = new DataFlowEdge(this, successor, state);
        successors.add(edge);
        edge.getDestination().getPredecessors().add(edge);
    }

    public void addSuccessor(@NotNull DataFlowBlock successor) {
        List<DataFlowState> states = getStates().stream()
                                                .map(state -> DataFlowState.createSuccessorState(successor, state))
                                                .toList();
        for (DataFlowState state : states) {
            addSuccessor(successor, state);
        }
    }

    @Override
    public String toString() {
        return "DataFlowBlock{" +
                "index=" + instruction.getIndex() +
                ", basicBlock=" + instruction +
                ", states=" + states +
                '}';
    }
}
