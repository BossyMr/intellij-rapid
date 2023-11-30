package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.*;
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
        separate(state, new BinaryExpression(BinaryOperator.EQUAL_TO, variable, expression));
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


    private void separate(@NotNull DataFlowState state, @NotNull Expression expression) {
        states.remove(state);
        List<DataFlowState> results = new ArrayList<>();
        results.add(state);
        for (Expression component : expression.getComponents()) {
            if (!(component instanceof IndexExpression indexExpression)) {
                continue;
            }
            indexExpression.iterate(child -> {
                if (child instanceof IndexExpression) {
                    List<DataFlowState> copy = new ArrayList<>();
                    for (DataFlowState result : results) {
                        copy.addAll(separate(result, ((IndexExpression) child)));
                    }
                    results.clear();
                    results.addAll(copy);
                }
            });
        }
        states.addAll(results);
    }

    private @NotNull List<DataFlowState> separate(@NotNull DataFlowState state, @NotNull IndexExpression expression) {
        List<DataFlowState> results = new ArrayList<>();
        ReferenceExpression variable = expression.getVariable();
        SnapshotExpression snapshot = state.getSnapshot(variable);
        if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
            return List.of();
        }
        List<ArrayEntry> assignments = arraySnapshot.getAssignments(state, expression.getIndex());
        if (assignments.size() == 1) {
            // The expression returns exactly one result.
            results.add(state);
            return results;
        }
        for (int i = 0; i < assignments.size(); i++) {
            ArrayEntry assignment = assignments.get(i);
            DataFlowState copy = DataFlowState.copy(state);
            // Constraint the index of the assignment to always be equal to the index for the expression.
            // As a result, this assignment will always be valid.
            assignAssignment(copy, expression, BinaryOperator.EQUAL_TO, assignment);
            for (int j = 0; j < i; j++) {
                // For any assignment which occurred after this assignment:
                // Constraint the index of the assignment to always be not equal to the index for the expression.
                // As a result, no assignment which occurred after this assignment will overwrite this assignment.
                assignAssignment(copy, expression, BinaryOperator.NOT_EQUAL_TO, assignments.get(j));
            }
            results.add(copy);
        }
        return results;
    }

    private void assignAssignment(@NotNull DataFlowState state, @NotNull IndexExpression indexValue, @NotNull BinaryOperator operator, @NotNull ArrayEntry entry) {
        if (!(entry instanceof ArrayEntry.Assignment assignment)) {
            return;
        }
        if (assignment.index() instanceof ReferenceExpression referenceValue) {
            /*
             * For this assignment to occur, the index for the assignment must match the specified index.
             */
            state.add(new BinaryExpression(operator, referenceValue, indexValue.getIndex()));
        } else if (indexValue.getIndex() instanceof ReferenceExpression referenceValue) {
            /*
             * Likewise, for the assignment to occur, the specified index must match the index for the assignment.
             * If the index for the assignment is not a variable, the above condition will not be added - so it must be
             * added now.
             */
            state.add(new BinaryExpression(operator, referenceValue, assignment.index()));
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
