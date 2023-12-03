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

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements SnapshotExpression {

    private final @NotNull RapidType type;
    private final @Nullable ReferenceExpression underlyingVariable;

    private final @NotNull Map<String, RapidType> components;
    private final @NotNull Map<String, Expression> snapshots;

    private final @NotNull Map<String, Expression> roots;

    public RecordSnapshot(@NotNull RapidType type, @Nullable ReferenceExpression underlyingVariable) {
        this.type = type;
        this.underlyingVariable = underlyingVariable;
        if (!(type.getRootStructure() instanceof RapidRecord record)) {
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
        this.roots = new HashMap<>();
    }

    public @NotNull Map<String, Expression> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<String, Expression> getRoots() {
        return roots;
    }

    public void assign(@NotNull String name, @NotNull Expression value) {
        snapshots.put(name, value);
        if (!(roots.containsKey(name))) {
            roots.put(name, value);
        }
    }

    public @NotNull Expression getValue(@NotNull String name) {
        if (!(snapshots.containsKey(name))) {
            throw new IllegalArgumentException();
        }
        return snapshots.get(name);
    }

    public @NotNull Expression getRoot(@NotNull String name) {
        if (!(roots.containsKey(name))) {
            throw new IllegalArgumentException();
        }
        return roots.get(name);
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
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "~" + hashCode();
    }
}
