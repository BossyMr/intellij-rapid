package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.ComponentValue;
import com.bossymr.rapid.language.flow.value.ReferenceSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.Value;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements ReferenceSnapshot {

    private final @NotNull ReferenceValue variable;

    private final @NotNull Map<String, RapidType> components;
    private final @NotNull Map<String, Value> snapshots;

    public RecordSnapshot(@NotNull ReferenceValue variable) {
        this.variable = variable;
        if (!(variable.getType().getTargetStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException();
        }
        this.components = new HashMap<>();
        for (RapidComponent component : record.getComponents()) {
            String componentName = component.getName();
            RapidType componentType = component.getType();
            if (componentName == null || componentType == null) {
                continue;
            }
            components.put(componentName, componentType);
        }
        this.snapshots = new HashMap<>();
    }

    public @NotNull Map<String, Value> getSnapshots() {
        return snapshots;
    }

    public void assign(@NotNull String name, @NotNull Value value) {
        snapshots.put(name, value);
    }

    public @NotNull VariableSnapshot createSnapshot(@NotNull String name) {
        if (!(components.containsKey(name))) {
            throw new IllegalArgumentException();
        }
        VariableSnapshot snapshot = new VariableSnapshot(components.get(name));
        snapshots.put(name, snapshot);
        return snapshot;
    }

    /**
     * Returns the latest snapshot for the component.
     *
     * @param component the component.
     * @return the snapshot.
     */
    public @NotNull Value getValue(@NotNull String name) {
        if (!(snapshots.containsKey(name))) {
            throw new IllegalArgumentException();
        }
        return snapshots.get(name);
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
