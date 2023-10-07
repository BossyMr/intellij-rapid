package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements SnapshotExpression {

    private final @NotNull RapidType type;
    private final @Nullable ReferenceExpression underlyingVariable;

    private final @NotNull Map<String, RapidType> components;
    private final @NotNull Map<String, Expression> snapshots;

    public RecordSnapshot(@NotNull RapidType type, @Nullable ReferenceExpression underlyingVariable) {
        this.type = type;
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
    public @Nullable ReferenceExpression getUnderlyingVariable() {
        return underlyingVariable;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitRecordSnapshotExpression(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        RecordSnapshot that = (RecordSnapshot) object;
        return Objects.equals(type, that.type) && Objects.equals(underlyingVariable, that.underlyingVariable) && Objects.equals(components, that.components) && Objects.equals(snapshots, that.snapshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, underlyingVariable, components, snapshots);
    }

    @Override
    public String toString() {
        return "~" + hashCode();
    }
}
