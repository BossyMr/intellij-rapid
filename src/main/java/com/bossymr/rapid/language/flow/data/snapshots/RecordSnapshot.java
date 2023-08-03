package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.ComponentValue;
import com.bossymr.rapid.language.flow.value.ReferenceSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements ReferenceSnapshot {

    private final @NotNull ReferenceValue variable;
    private final @NotNull Map<ComponentValue, VariableSnapshot> snapshots;

    public RecordSnapshot(@NotNull ReferenceValue variable) {
        this.variable = variable;
        this.snapshots = new HashMap<>();
    }

    public @NotNull Map<ComponentValue, VariableSnapshot> getSnapshots() {
        return snapshots;
    }

    /**
     * Creates a new snapshot for the specified component.
     *
     * @param component the component.
     * @return the snapshot.
     */
    public @NotNull VariableSnapshot createSnapshot(@NotNull ComponentValue component) {
        component = getComponentValue(component);
        VariableSnapshot snapshot = new VariableSnapshot(component);
        snapshots.put(component, snapshot);
        return snapshot;
    }

    /**
     * Returns the latest snapshot for the component.
     *
     * @param component the component.
     * @return the snapshot.
     */
    public @NotNull VariableSnapshot getSnapshot(@NotNull ComponentValue component) {
        component = getComponentValue(component);
        if (!(snapshots.containsKey(component))) {
            throw new IllegalArgumentException();
        }
        return snapshots.get(component);
    }

    /**
     * Returns the constraint for the component.
     *
     * @param state the state.
     * @param component the component.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull DataFlowState state, @NotNull ComponentValue component) {
        VariableSnapshot snapshot = getSnapshot(component);
        return state.getConstraint(snapshot);
    }

    private @NotNull ComponentValue getComponentValue(@NotNull ComponentValue componentValue) {
        ReferenceValue referenceValue = componentValue.variable();
        if (referenceValue.equals(variable)) {
            return new ComponentValue(componentValue.getType(), this, componentValue.name());
        } else if (referenceValue.equals(this)) {
            return componentValue;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public @NotNull ReferenceValue getVariable() {
        return variable;
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitRecordSnapshot(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return variable.getType();
    }
}
