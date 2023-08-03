package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.StringConstraint;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
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

    private final @NotNull Set<DataFlowEdge> successors = new HashSet<>();
    private final @NotNull Set<DataFlowEdge> predecessors = new HashSet<>();

    public DataFlowBlock(@NotNull BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
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
            state.assign(condition);
        }
    }

    private void separate(@NotNull Condition condition) {
        List<ReferenceValue> variables = condition.getVariables();
        for (ReferenceValue referenceValue : variables) {
            List<IndexValue> indexValues = getIndexValues(referenceValue);
            for (ListIterator<IndexValue> iterator = indexValues.listIterator(indexValues.size()); iterator.hasPrevious(); ) {
                List<DataFlowState> results = separate(iterator.previous());
                states.clear();
                states.addAll(results);
            }
        }
    }

    private @NotNull List<DataFlowState> separate(@NotNull IndexValue indexValue) {
        List<DataFlowState> results = new ArrayList<>();
        for (DataFlowState state : states) {
            ReferenceSnapshot snapshot = state.getSnapshot(indexValue.variable());
            if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                throw new IllegalStateException();
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
            state.add(new Condition(referenceValue, conditionType, new ValueExpression(indexValue.index())));
        } else if (indexValue.index() instanceof ReferenceValue referenceValue) {
            /*
             * Likewise, for the assignment to occur, the specified index must match the index for the assignment.
             * If the index for the assignment is not a variable, the above condition will not be added - so it must be
             * added now. However, if the above condition is added, the condition will be solved, and this condition
             * will be added automatically.
             */
            state.add(new Condition(referenceValue, conditionType, new ValueExpression(assignment.index())));
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
            if (type.isAssignable(RapidType.NUMBER) || type.isAssignable(RapidType.DOUBLE)) {
                return NumericConstraint.equalTo((double) constantValue.value());
            }
            if (type.isAssignable(RapidType.STRING)) {
                return StringConstraint.anyOf((String) constantValue.value());
            }
            if (type.isAssignable(RapidType.BOOLEAN)) {
                return BooleanConstraint.equalTo((boolean) constantValue.value());
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
            for (Condition condition : state.getConditions(instruction.variable())) {
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
        List<DataFlowState> states = split(condition);
        successors.add(new DataFlowEdge(this, successor, states));
    }

    public @NotNull List<DataFlowState> split(@NotNull Condition condition) {
        return getStates().stream()
                .filter(state -> state.intersects(condition))
                .map(DataFlowState::copy)
                .peek(state -> state.add(condition))
                .toList();
    }

    public void addSuccessor(@NotNull DataFlowBlock successor) {
        List<DataFlowState> states = getStates().stream()
                .map(DataFlowState::copy)
                .toList();
        successors.add(new DataFlowEdge(this, successor, states));
    }

    public void addSuccessor(@NotNull DataFlowBlock successor, @NotNull List<DataFlowState> states) {
        successors.add(new DataFlowEdge(this, successor, states));
    }

}
