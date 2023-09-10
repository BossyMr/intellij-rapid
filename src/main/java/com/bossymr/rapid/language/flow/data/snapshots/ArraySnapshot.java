package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

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
public class ArraySnapshot implements SnapshotExpression {

    private final @NotNull ReferenceExpression underlyingVariable;
    private final @NotNull Function<ArraySnapshot, Expression> defaultValue;
    private final @NotNull List<ArrayEntry.Assignment> assignments;

    public ArraySnapshot(@NotNull Function<ArraySnapshot, Expression> defaultValue, @NotNull ReferenceExpression underlyingVariable) {
        this.underlyingVariable = underlyingVariable;
        this.defaultValue = defaultValue;
        this.assignments = new ArrayList<>();
    }

    public @NotNull Supplier<Expression> getDefaultValue() {
        return () -> defaultValue.apply(this);
    }

    public @NotNull List<ArrayEntry.Assignment> getAssignments() {
        return assignments;
    }

    public void assign(@NotNull Expression index, @NotNull Expression value) {
        assignments.add(new ArrayEntry.Assignment(index, value));
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull Expression index) {
        RapidType elementType = getType().createArrayType(getType().getDimensions() - 1);
        SnapshotExpression snapshot = createSnapshot(elementType, index);
        assignments.add(new ArrayEntry.Assignment(index, snapshot));
        return snapshot;
    }

    private @NotNull SnapshotExpression createSnapshot(@NotNull RapidType elementType, @NotNull Expression index) {
        IndexExpression indexValue = new IndexExpression(underlyingVariable, index);
        if (elementType.getDimensions() > 0) {
            return new ArraySnapshot(defaultValue, indexValue);
        } else if (elementType.getActualStructure() instanceof RapidRecord) {
            return new RecordSnapshot(indexValue);
        } else {
            return new VariableSnapshot(indexValue);
        }
    }

    public @NotNull List<ArrayEntry> getAssignments(@NotNull DataFlowState state, @NotNull Expression index) {
        List<ArrayEntry> values = new ArrayList<>();
        loop:
        for (ListIterator<ArrayEntry.Assignment> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            ArrayEntry.Assignment assignment = iterator.previous();
            BooleanValue constraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, assignment.index(), index));
            if (constraint == BooleanValue.ALWAYS_FALSE || constraint == BooleanValue.NO_VALUE) {
                // The specified index cannot be equal to the current assignment, as such, this assignment should not be considered.
                continue;
            }
            for (ArrayEntry previousEntry : values) {
                if (previousEntry instanceof ArrayEntry.Assignment assignmentEntry) {
                    BooleanValue comparisonConstraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, assignmentEntry.index(), assignment.index()));
                    if (comparisonConstraint == BooleanValue.ALWAYS_TRUE) {
                        // The index of the current assignment is always equal to the index of an assignment already considered, as a result, that entry would always replace this entry.
                        continue loop;
                    }
                }
            }
            values.add(assignment);
            if (constraint == BooleanValue.ALWAYS_TRUE) {
                // The specified index is always equal to the current assignment, as such, this assignment must be returned; while older assignments would be overwritten.
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
    public @NotNull ReferenceExpression getUnderlyingVariable() {
        return underlyingVariable;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitArraySnapshotExpression(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return underlyingVariable.getType();
    }

    @Override
    public String toString() {
        return "ArraySnapshot{" +
                "variable=" + underlyingVariable +
                ", defaultValue=" + defaultValue +
                ", assignments=" + assignments +
                '}';
    }
}
