package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.StringConstraint;
import com.bossymr.rapid.language.flow.data.BlockCycle;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
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

    public void assign(@NotNull Condition condition) {
        if (condition.getVariable() instanceof FieldValue) {
            return;
        }
        separate(condition);
        for (DataFlowState state : states) {
            if (isCyclic(state, condition)) {
                /*
                 * If the assignment is cyclic, we need to rewrite the assignment as a function of the index. If the
                 * assignment cannot be rewritten, assume that the value can be anything. We need to do this, otherwise
                 * we would need as many states as there might be iterations of the loop, which could be infinite.
                 */
                state.createSnapshot(condition.getVariable());
            } else {
                state.assign(condition, true);
            }
        }
    }

    /**
     * Checks if the specified condition is cyclic, for the specified state and condition. A condition is cyclic if
     * this block is part of a cycle (loop) and the expression of the condition references the variable of the
     * condition. If the expression references a previous snapshot of the variable, it is still counted as the same
     * variable. The condition is recursively searched, so if the expression contains a variable which itself references
     * the variable, it is still cyclic.
     *
     * @param state the state.
     * @param condition the condition.
     * @return if the specified condition is cyclic.
     */
    public boolean isCyclic(@NotNull DataFlowState state, @NotNull Condition condition) {
        if (!(getStates().contains(state))) {
            throw new IllegalArgumentException("State: " + state + " must be in block: " + this);
        }
        if (getCycles().isEmpty()) {
            /*
             * If this block is not part of a cycle, the variable would only be modified once.
             */
            return false;
        }
        return isCyclic(condition.getVariable(), condition, state, new HashSet<>());
    }

    private boolean isCyclic(@NotNull ReferenceValue variable, @NotNull Condition condition, @NotNull DataFlowState state, @NotNull Set<Condition> visited) {
        if (!(visited.add(condition))) {
            return false;
        }
        List<ReferenceValue> variables = condition.getVariables();
        for (ReferenceValue referenceValue : variables) {
            if (referenceValue instanceof ReferenceSnapshot snapshot) {
                Optional<ReferenceValue> optional = state.getVariable(snapshot);
                if (optional.isPresent()) {
                    referenceValue = optional.orElseThrow();
                }
            }
            if (referenceValue.equals(variable)) {
                return true;
            }
            Set<Condition> conditions = state.findConditions(referenceValue);
            for (Condition child : conditions) {
                if (isCyclic(variable, child, state, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void separate(@NotNull Condition condition) {
        List<ReferenceValue> variables = condition.getVariables();
        List<DataFlowState> copy = new ArrayList<>();
        for (ReferenceValue referenceValue : variables) {
            List<IndexValue> indexValues = getIndexValues(referenceValue);
            for (ListIterator<IndexValue> iterator = indexValues.listIterator(indexValues.size()); iterator.hasPrevious(); ) {
                List<DataFlowState> results = separate(iterator.previous());
                copy.addAll(results);
            }
        }
        if (copy.isEmpty()) {
            return;
        }
        System.out.println("Separating on: " + condition + ": " + states.size() + " -> " + copy.size());
        states.clear();
        states.addAll(copy);
    }

    private @NotNull List<DataFlowState> separate(@NotNull IndexValue indexValue) {
        if (indexValue.variable() instanceof FieldValue) {
            return List.copyOf(states);
        }
        List<DataFlowState> results = new ArrayList<>();
        for (DataFlowState state : states) {
            Optional<ReferenceSnapshot> snapshot = state.findSnapshot(indexValue.variable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                continue;
            }
            List<ArrayEntry> assignments = arraySnapshot.getAssignments(state, indexValue.index());
            if (assignments.size() == 1) {
                results.add(state);
                continue;
            }
            for (int i = 0; i < assignments.size(); i++) {
                ArrayEntry assignment = assignments.get(i);
                DataFlowState copy = DataFlowState.copy(state);
                assignAssignment(copy, indexValue, ConditionType.EQUALITY, assignment);
                for (int j = 0; j < i; j++) {
                    assignAssignment(copy, indexValue, ConditionType.INEQUALITY, assignments.get(j));
                }
                results.add(copy);
            }
        }
        return results;
    }

    private void assignAssignment(@NotNull DataFlowState state, @NotNull IndexValue indexValue, @NotNull ConditionType conditionType, @NotNull ArrayEntry entry) {
        if (!(entry instanceof ArrayEntry.Assignment assignment)) {
            return;
        }
        if (assignment.index() instanceof ReferenceValue referenceValue) {
            /*
             * For this assignment to occur, the index for the assignment must match the specified index.
             */
            state.add(new Condition(referenceValue, conditionType, new ValueExpression(indexValue.index())), true);
        } else if (indexValue.index() instanceof ReferenceValue referenceValue) {
            /*
             * Likewise, for the assignment to occur, the specified index must match the index for the assignment.
             * If the index for the assignment is not a variable, the above condition will not be added - so it must be
             * added now. However, if the above condition is added, the condition will be solved, and this condition
             * will be added automatically.
             */
            state.add(new Condition(referenceValue, conditionType, new ValueExpression(assignment.index())), true);
        }
    }

    private @NotNull List<IndexValue> getIndexValues(@NotNull ReferenceValue referenceValue) {
        if (!(referenceValue instanceof IndexValue)) {
            return List.of();
        }
        List<IndexValue> indexValues = new ArrayList<>();
        while (referenceValue instanceof IndexValue indexValue) {
            indexValues.add(indexValue);
            referenceValue = indexValue.variable();
        }
        return indexValues;
    }

    public @NotNull Constraint getConstraint(@NotNull Value value) {
        if (value instanceof ReferenceValue referenceValue) {
            return getConstraint(referenceValue);
        } else if (value instanceof ConstantValue constantValue) {
            RapidType type = constantValue.getType();
            if (type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                return NumericConstraint.equalTo((double) constantValue.getValue());
            }
            if (type.isAssignable(RapidPrimitiveType.STRING)) {
                return StringConstraint.anyOf((String) constantValue.getValue());
            }
            if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
                return BooleanConstraint.equalTo((boolean) constantValue.getValue());
            }
        } else if (value instanceof ErrorValue) {
            return Constraint.any(value.getType());
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    private @NotNull Constraint getConstraint(@NotNull ReferenceValue referenceValue) {
        return states.stream()
                .map(state -> state.getConstraint(referenceValue))
                .collect(Constraint.or(referenceValue.getType()));
    }

    public @NotNull Constraint getHistoricConstraint(@NotNull ReferenceValue value, @NotNull LinearInstruction.AssignmentInstruction instruction) {
        List<Constraint> constraints = new ArrayList<>();
        for (DataFlowState state : states) {
            for (Condition condition : state.findConditions(instruction.variable())) {
                Class<?> conditionClass = condition.getExpression().getClass();
                Class<?> assignmentClass = instruction.value().getClass();
                if (!(conditionClass.equals(assignmentClass))) {
                    continue;
                }
                for (ReferenceValue variable : condition.getVariables()) {
                    if (!(variable instanceof VariableSnapshot snapshot)) {
                        continue;
                    }
                    if (!(Objects.equals(snapshot.getVariable(), value))) {
                        continue;
                    }
                    Constraint constraint = state.getConstraint(snapshot);
                    constraints.add(constraint);
                }
            }
        }
        if (constraints.isEmpty()) {
            return Constraint.any(value.getType());
        }
        return Constraint.or(constraints);
    }

    public void addSuccessor(@NotNull DataFlowBlock successor, @NotNull Condition condition) {
        List<DataFlowState> states = split(successor, condition);
        successors.add(new DataFlowEdge(this, successor, states));
    }

    public @NotNull List<DataFlowState> split(@NotNull DataFlowBlock successor, @NotNull Condition condition) {
        List<DataFlowState> flowStates = getStates();
        return flowStates.stream()
                .filter(state -> state.contains(condition))
                .map(state -> DataFlowState.createSuccessorState(successor, state))
                .peek(state -> state.add(condition, true))
                .toList();
    }

    public void addSuccessor(@NotNull DataFlowBlock successor) {
        List<DataFlowState> states = getStates().stream()
                .map(state -> DataFlowState.createSuccessorState(successor, state))
                .toList();
        successors.add(new DataFlowEdge(this, successor, states));
    }

    public void addSuccessor(@NotNull DataFlowBlock successor, @NotNull List<DataFlowState> states) {
        successors.add(new DataFlowEdge(this, successor, states));
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
