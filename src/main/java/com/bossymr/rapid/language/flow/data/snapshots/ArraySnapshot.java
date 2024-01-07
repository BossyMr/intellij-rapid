package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A snapshot which represents an array assignment.
 */
public class ArraySnapshot implements Snapshot {

    private final @Nullable Snapshot parent;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;
    private final @NotNull DefaultValueProvider defaultValue;
    private final @NotNull List<Entry> assignments = new ArrayList<>();
    private final @NotNull Snapshot length = Snapshot.createSnapshot(RapidPrimitiveType.NUMBER);

    public ArraySnapshot(@Nullable Snapshot parent, @NotNull RapidType type, @NotNull Optionality optionality, @NotNull DefaultValueProvider defaultValue) {
        if (!(type.isArray())) {
            throw new IllegalArgumentException("Cannot create array snapshot for variable of type: " + type);
        }
        this.parent = parent;
        this.type = type;
        this.optionality = optionality;
        this.defaultValue = defaultValue;
    }

    @Override
    public @Nullable Snapshot getParent() {
        return parent;
    }

    public @NotNull Snapshot getLength() {
        return length;
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull List<Entry> getAssignments() {
        return assignments;
    }

    public @NotNull Snapshot assign(@NotNull DataFlowState state, @NotNull Expression index) {
        RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
        Snapshot snapshot = Snapshot.createSnapshot(arrayType, this);
        assignments.add(new Entry(state, index, snapshot));
        return snapshot;
    }

    public @Nullable ArraySnapshot.Entry getAssignment(@NotNull Snapshot snapshot) {
        for (Entry assignment : assignments) {
            if (assignment.snapshot().equals(snapshot)) {
                return assignment;
            }
        }
        return null;
    }

    public @NotNull List<Entry> getAssignments(@NotNull DataFlowState state) {
        List<Entry> values = new ArrayList<>();
        for (ListIterator<Entry> iterator = assignments.listIterator(getValidAssignment(state) + 1); iterator.hasPrevious(); ) {
            Entry assignment = iterator.previous();
            if (contains(values, assignment, state)) {
                // A snapshot with the same index was assigned after this index.
                continue;
            }
            values.add(assignment);
        }
        return values;
    }

    private int getValidAssignment(@NotNull DataFlowState state) {
        for (int i = assignments.size() - 1; i >= 0; i--) {
            Entry assignment = assignments.get(i);
            if (state.isAncestor(assignment.state())) {
                return i;
            }
        }
        return -1;
    }

    public @NotNull List<Entry> getAssignments(@NotNull DataFlowState state, @NotNull Expression index) {
        List<Entry> values = new ArrayList<>();
        for (ListIterator<Entry> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            Entry assignment = iterator.previous();
            BooleanValue constraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, assignment.index(), index));
            if (constraint == BooleanValue.ALWAYS_FALSE || constraint == BooleanValue.NO_VALUE) {
                continue;
            }
            if (constraint == BooleanValue.ALWAYS_TRUE) {
                values.add(assignment);
                break;
            }
            if (contains(values, assignment, state)) {
                continue;
            }
            values.add(assignment);
            if (!(iterator.hasPrevious())) {
                Entry entry = new Entry(state, index, defaultValue.getDefaultValue(this, state));
                assignments.add(entry);
                values.add(entry);
            }
        }
        if (values.isEmpty()) {
            Entry assignment = new Entry(state, index, defaultValue.getDefaultValue(this, state));
            assignments.add(assignment);
            values.add(assignment);
        }
        return values;
    }

    private boolean contains(@NotNull List<Entry> assignments, @NotNull ArraySnapshot.Entry nextAssignment, @NotNull DataFlowState state) {
        for (Entry assignment : assignments) {
            BooleanValue comparisonConstraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, assignment.index(), nextAssignment.index()));
            if (comparisonConstraint == BooleanValue.ALWAYS_TRUE) {
                // The index of this assignment, which was made after nextAssignment, is always equal to the index of nextAssignment.
                // As a result, it is no longer possible for the value of nextAssignment to be returned by this index expression.
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    public @NotNull RapidType getArrayType() {
        return type.createArrayType(type.getDimensions() - 1);
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "~" + hashCode() + "[" + switch (getOptionality()) {
            case PRESENT -> "P";
            case UNKNOWN -> "P/M";
            case MISSING -> "M";
            case NO_VALUE -> "";
        } + "]";
    }

    /**
     * A provider responsible for creating snapshots equal to the default value of this array.
     */
    @FunctionalInterface
    public interface DefaultValueProvider {

        /**
         * Creates a new snapshot equal to the default value of this array.
         *
         * @param snapshot the current array.
         * @param state the current state.
         * @return a new snapshot.
         * @implSpec the created snapshot must be assignable to the array type of the specified array.
         */
        @NotNull Snapshot computeDefaultValue(@NotNull ArraySnapshot snapshot, @NotNull DataFlowState state);

        private @NotNull Snapshot getDefaultValue(@NotNull ArraySnapshot snapshot, @NotNull DataFlowState state) {
            Snapshot defaultValue = computeDefaultValue(snapshot, state);
            if (!(defaultValue.getType().isAssignable(snapshot.getArrayType()))) {
                throw new IllegalStateException("The default value computed for the array: " + snapshot + " is of type: " + defaultValue.getType() + " which is not assignable to its array type: " + snapshot.getArrayType());
            }
            return defaultValue;
        }
    }

    public record Entry(@NotNull DataFlowState state, @NotNull Expression index, @NotNull Snapshot snapshot) {}
}
