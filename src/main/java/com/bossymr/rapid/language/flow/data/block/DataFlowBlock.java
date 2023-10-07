package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.BlockCycle;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@code DataFlowBlock} represents the state of the program at the end of a specific block. A {@code DataFlowBlock}
 * contains references to predecessors and successors, as-well as a series of states which represent the possible states
 * of the program.
 */
public class DataFlowBlock {

    private final @NotNull BasicBlock basicBlock;
    private final @NotNull List<DataFlowState> states = new ArrayList<>();

    private final @NotNull Set<BlockCycle> cycles;

    private final @NotNull Set<DataFlowEdge> successors = new HashSet<>();
    private final @NotNull Set<DataFlowEdge> predecessors = new HashSet<>();

    public DataFlowBlock(@NotNull BasicBlock basicBlock, @NotNull Set<BlockCycle> cycles) {
        this.basicBlock = basicBlock;
        this.cycles = cycles;
    }

    public @NotNull Set<BlockCycle> getCycles() {
        return cycles;
    }

    public @NotNull Set<BlockCycle> getHeads() {
        Set<BlockCycle> counters = new HashSet<>();
        for (BlockCycle blockCycle : cycles) {
            List<DataFlowBlock> sequence = blockCycle.getSequence();
            if (sequence.isEmpty()) {
                continue;
            }
            if (sequence.get(0).equals(this)) {
                counters.add(blockCycle);
            }
        }
        return Set.copyOf(counters);
    }


    public @NotNull Set<BlockCycle> getTails() {
        Set<BlockCycle> counters = new HashSet<>();
        for (BlockCycle blockCycle : cycles) {
            List<DataFlowBlock> sequence = blockCycle.getSequence();
            if (sequence.isEmpty()) {
                continue;
            }
            if (sequence.get(sequence.size() - 1).equals(this)) {
                counters.add(blockCycle);
            }
        }
        return Set.copyOf(counters);
    }

    public @NotNull BasicBlock getBasicBlock() {
        return basicBlock;
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

    public void add(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot assign expression: " + expression);
        }
        separate(expression);
        for (DataFlowState state : states) {
            // TODO: Check whether this assignment is cyclic, and if so, handle it correctly by using a relevant path counter.
            state.add(expression);
        }
    }

    public void assign(@NotNull ReferenceExpression variable, @NotNull Expression expression) {
        if (!(variable.getType().isAssignable(expression.getType()))) {
            throw new IllegalArgumentException("Cannot assign expression: " + expression);
        }
        separate(new BinaryExpression(BinaryOperator.EQUAL_TO, variable, expression));
        for (DataFlowState state : states) {
            // TODO: Check whether this assignment is cyclic, and if so, handle it correctly by using a relevant path counter.
            state.assign(variable, expression);
        }
    }

    private void separate(@NotNull Expression expression) {
        // TODO: 2023-09-10 This entire algorithm seems a bit overly complicated
        Collection<Expression> components = expression.getComponents();
        List<DataFlowState> copy = new ArrayList<>();
        for (Expression component : components) {
            if (!(component instanceof ReferenceExpression referenceExpression)) {
                continue;
            }
            List<IndexExpression> indexExpressions = getIndexExpressions(referenceExpression);
            for (ListIterator<IndexExpression> iterator = indexExpressions.listIterator(indexExpressions.size()); iterator.hasPrevious(); ) {
                List<DataFlowState> results = separate(iterator.previous());
                copy.addAll(results);
            }
        }
        if (copy.isEmpty()) {
            return;
        }
        states.clear();
        states.addAll(copy);
    }

    private @NotNull List<DataFlowState> separate(@NotNull IndexExpression indexValue) {
        if (indexValue.getVariable() instanceof FieldExpression) {
            return List.copyOf(states);
        }
        List<DataFlowState> results = new ArrayList<>();
        for (DataFlowState state : states) {
            Optional<SnapshotExpression> snapshot = state.getSnapshot(indexValue.getVariable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                continue;
            }
            List<ArrayEntry> assignments = arraySnapshot.getAssignments(state, indexValue.getIndex());
            if (assignments.size() == 1) {
                results.add(state);
                continue;
            }
            for (int i = 0; i < assignments.size(); i++) {
                ArrayEntry assignment = assignments.get(i);
                DataFlowState copy = DataFlowState.copy(state);
                assignAssignment(copy, indexValue, BinaryOperator.EQUAL_TO, assignment);
                for (int j = 0; j < i; j++) {
                    assignAssignment(copy, indexValue, BinaryOperator.NOT_EQUAL_TO, assignments.get(j));
                }
                results.add(copy);
            }
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

    private @NotNull List<IndexExpression> getIndexExpressions(@NotNull ReferenceExpression referenceValue) {
        if (!(referenceValue instanceof IndexExpression)) {
            return List.of();
        }
        List<IndexExpression> indexValues = new ArrayList<>();
        while (referenceValue instanceof IndexExpression indexValue) {
            indexValues.add(indexValue);
            referenceValue = indexValue.getVariable();
        }
        return indexValues;
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression variable, @NotNull PsiElement element) {
        Optionality optionality = null;
        for (DataFlowState state : states) {
            Optional<SnapshotExpression> snapshot = state.getHistoricSnapshot(variable, element);
            ReferenceExpression expression = snapshot.isPresent() ? snapshot.orElseThrow() : variable;
            if(optionality == null) {
                optionality = state.getOptionality(expression);
            } else {
                optionality = optionality.or(state.getOptionality(expression));
            }
        }
        if(optionality == null) {
            throw new IllegalStateException("Invalid block: " + this);
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
        if (state.getBlock().isPresent()) {
            if(state.getBlock().orElseThrow() != successor) {
                state = DataFlowState.createSuccessorState(successor, state);
            }
        }
        DataFlowEdge edge = new DataFlowEdge(this, successor, state);
        successors.add(edge);
    }

    public void addSuccessor(@NotNull DataFlowBlock successor) {
        List<DataFlowState> states = getStates().stream()
                .map(state -> DataFlowState.createSuccessorState(successor, state))
                .toList();
        for (DataFlowState state : states) {
            successors.add(new DataFlowEdge(this, successor, state));
        }
    }

    @Override
    public String toString() {
        return "DataFlowBlock{" +
                "index=" + basicBlock.getIndex() +
                ", basicBlock=" + basicBlock +
                ", states=" + states +
                '}';
    }
}
