package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A snapshot based on an array. The index to assign a value does not need to be concrete; as a result, every
 * assignment is stored separately. This also leads to uncertainty as to which assignment actually modified a specific
 * index, as a result, the state needs to be split for each retrieval.
 * <pre>{@code
 * num a{*};    // a -> a1 {x -> 0}
 *              // x -> any
 * a{2} := 2;   // a1 {2 -> 2}
 * y -> [0, 10]
 * a{y} -> 5;   // a1 {y1 -> 5}                     // State #1     // State #2
 * z = a{2};    // a1 {x -> 0, 2 -> 2, y1 -> 5}     // z = 5        // z = 2
 *              // y1 might match 2                 // y1 = 2       // y1 != 2
 *              // 2 always matches 2
 *              // z = 5 or 2
 * }</pre>
 */
public class ArraySnapshot implements ReferenceSnapshot {

    private final @NotNull ReferenceValue variable;
    private final @NotNull Value defaultValue;
    private final @NotNull List<ArrayEntry.Assignment> assignments;

    public ArraySnapshot(@NotNull Value defaultValue, @NotNull ReferenceValue variable) {
        this.variable = variable;
        this.defaultValue = defaultValue;
        this.assignments = new ArrayList<>();
    }

    public @NotNull Value getDefaultValue() {
        return defaultValue;
    }

    public @NotNull List<ArrayEntry.Assignment> getAssignments() {
        return assignments;
    }

    public void assign(@NotNull Value index, @NotNull Value value) {
        assignments.add(new ArrayEntry.Assignment(index, value));
    }

    public @NotNull ReferenceSnapshot createSnapshot(@NotNull Value index) {
        RapidType elementType = getType().createArrayType(getType().getDimensions() - 1);
        ReferenceSnapshot snapshot = createSnapshot(elementType, index);
        assignments.add(new ArrayEntry.Assignment(index, snapshot));
        return snapshot;
    }

    private @NotNull ReferenceSnapshot createSnapshot(@NotNull RapidType elementType, @NotNull Value index) {
        IndexValue indexValue = new IndexValue(variable, index);
        if (elementType.getDimensions() > 0) {
            return new ArraySnapshot(defaultValue, indexValue);
        } else if (elementType.getTargetStructure() instanceof RapidRecord) {
            return new RecordSnapshot(indexValue);
        } else {
            return new VariableSnapshot(indexValue);
        }
    }

    public @NotNull List<ArrayEntry> getAssignments(@NotNull DataFlowState state, @NotNull Constraint constraint) {
        List<ArrayEntry> values = new ArrayList<>();
        for (ListIterator<ArrayEntry.Assignment> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            ArrayEntry.Assignment assignment = iterator.previous();
            Constraint indexConstraint = state.getConstraint(assignment.index());
            if (!(indexConstraint.intersects(constraint))) {
                continue;
            }
            values.add(assignment);
            if (indexConstraint.getValue().isPresent() && indexConstraint.getValue().equals(constraint.getValue())) {
                break;
            }
            if (!(iterator.hasPrevious())) {
                values.add(new ArrayEntry.DefaultValue(defaultValue));
            }
        }
        return values;
    }

    public @NotNull List<ArrayEntry> getAssignments(@NotNull DataFlowState state, @NotNull Value index) {
        List<ArrayEntry> values = new ArrayList<>();
        for (ListIterator<ArrayEntry.Assignment> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            ArrayEntry.Assignment assignment = iterator.previous();
            Constraint constraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, assignment.index(), index));
            if (constraint.equals(BooleanConstraint.alwaysFalse())) {
                continue;
            }
            for (ArrayEntry value : values) {
                if(value instanceof ArrayEntry.Assignment assignmentEntry) {
                    Constraint comparisonConstraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, assignmentEntry.index(), assignment.index()));
                    if(comparisonConstraint.equals(BooleanConstraint.alwaysTrue())) {
                        break;
                    }
                }
            }
            values.add(assignment);
            if (constraint.equals(BooleanConstraint.alwaysTrue())) {
                break;
            }
            if (!(iterator.hasPrevious())) {
                values.add(new ArrayEntry.DefaultValue(defaultValue));
            }
        }
        if (values.isEmpty()) {
            values.add(new ArrayEntry.DefaultValue(defaultValue));
        }
        return values;
    }

    @Override
    public @NotNull ReferenceValue getVariable() {
        return variable;
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitArraySnapshot(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return variable.getType();
    }

    @Override
    public String toString() {
        return "ArraySnapshot{" +
                "variable=" + variable +
                ", defaultValue=" + defaultValue +
                ", assignments=" + assignments +
                '}';
    }
}
