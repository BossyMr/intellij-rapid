package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements SnapshotExpression {

    private final @NotNull ReferenceExpression underlyingVariable;

    private final @NotNull Map<String, RapidType> components;
    private final @NotNull Map<String, Expression> snapshots;

    public RecordSnapshot(@NotNull ReferenceExpression underlyingVariable) {
        this.underlyingVariable = underlyingVariable;
        if (!(underlyingVariable.getType().getActualStructure() instanceof RapidRecord record)) {
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

    public @NotNull Map<String, Expression> getSnapshots() {
        return snapshots;
    }

    public void assign(@NotNull String name, @NotNull Expression value) {
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

    public @NotNull Expression getValue(@NotNull String name) {
        if (!(snapshots.containsKey(name))) {
            throw new IllegalArgumentException();
        }
        return snapshots.get(name);
    }

    @Override
    public @NotNull ReferenceExpression getUnderlyingVariable() {
        return underlyingVariable;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitRecordSnapshotExpression(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return underlyingVariable.getType();
    }
}
