package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
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
public class ArraySnapshot implements Snapshot {

    private final @Nullable Snapshot snapshot;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;
    private final @NotNull DefaultValueProvider defaultValue;
    private final @NotNull List<Entry> assignments;
    private final @NotNull Snapshot length;

    public ArraySnapshot(@Nullable Snapshot snapshot, @NotNull RapidType type, @NotNull Optionality optionality, @NotNull DefaultValueProvider defaultValue) {
        this.snapshot = snapshot;
        this.type = type;
        this.optionality = optionality;
        this.defaultValue = defaultValue;
        this.assignments = new ArrayList<>();
        this.length = Snapshot.createSnapshot(RapidPrimitiveType.NUMBER);
    }

    @Override
    public @Nullable Snapshot getParent() {
        return snapshot;
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

    public void assign(@NotNull DataFlowState state, @NotNull Expression index, @NotNull Snapshot snapshot) {
        assignments.add(new Entry(state, index, snapshot));
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
        boolean canContinue = false;
        for (ListIterator<Entry> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            Entry assignment = iterator.previous();
            if (!(canContinue)) {
                if (!(canUseAssignment(assignment, state))) {
                    continue;
                }
                canContinue = true;
            }
            if (isDuplicate(values, assignment, state)) {
                // A snapshot with the same index was assigned after this index.
                continue;
            }
            values.add(assignment);
        }
        return values;
    }

    private boolean canUseAssignment(@NotNull ArraySnapshot.Entry assignment, @NotNull DataFlowState state) {
        for (DataFlowState predecessor = state; predecessor != null; predecessor = predecessor.getPredecessor()) {
            if (predecessor.equals(assignment.state())) {
                return true;
            }
        }
        return false;
    }

    public @NotNull List<Entry> getAssignments(@NotNull DataFlowState state, @NotNull Expression index) {
        List<Entry> values = new ArrayList<>();
        for (ListIterator<Entry> iterator = assignments.listIterator(assignments.size()); iterator.hasPrevious(); ) {
            Entry assignment = iterator.previous();
            BooleanValue constraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, assignment.index(), index));
            if (constraint == BooleanValue.ALWAYS_FALSE || constraint == BooleanValue.NO_VALUE) {
                // The specified index cannot be equal to the current assignment, as such, this assignment should not be considered.
                continue;
            }
            if (isDuplicate(values, assignment, state)) {
                continue;
            }
            values.add(assignment);
            if (constraint == BooleanValue.ALWAYS_TRUE) {
                // The specified index is always equal to the current assignment, as such, this assignment must be returned; while older assignments would be overwritten.
                break;
            }
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

    private boolean isDuplicate(@NotNull List<Entry> entries, @NotNull ArraySnapshot.Entry entry, @NotNull DataFlowState state) {
        for (Entry previousEntry : entries) {
            BooleanValue comparisonConstraint = state.getConstraint(new BinaryExpression(BinaryOperator.EQUAL_TO, previousEntry.index(), entry.index()));
            if (comparisonConstraint == BooleanValue.ALWAYS_TRUE) {
                // The index of the current assignment is always equal to the index of an assignment already considered, as a result, that entry would always replace this entry.
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
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

    @FunctionalInterface
    public interface DefaultValueProvider {
        @NotNull Snapshot getDefaultValue(@NotNull ArraySnapshot snapshot, @NotNull DataFlowState state);
    }

    public record Entry(@NotNull DataFlowState state, @NotNull Expression index, @NotNull Snapshot snapshot) {}
}
