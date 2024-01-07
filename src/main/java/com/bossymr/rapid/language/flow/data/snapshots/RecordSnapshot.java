package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements Snapshot {

    private final @Nullable Snapshot parent;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;
    private final @NotNull Map<String, RapidType> components;
    private final @NotNull DefaultValueProvider defaultValue;
    private final @NotNull Map<String, List<Entry>> snapshots;

    public RecordSnapshot(@Nullable Snapshot parent, @NotNull RapidType type, @NotNull Optionality optionality, @NotNull DefaultValueProvider defaultValue) {
        this.parent = parent;
        this.type = type;
        this.optionality = optionality;
        this.defaultValue = defaultValue;
        if (!(type.getRootStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException();
        }
        this.components = new HashMap<>();
        for (RapidComponent component : record.getComponents()) {
            String componentName = component.getName();
            RapidType componentType = component.getType();
            if (componentName == null) {
                continue;
            }
            components.put(componentName, Objects.requireNonNullElse(componentType, RapidPrimitiveType.ANYTYPE));
        }
        this.snapshots = new HashMap<>();
    }

    @Override
    public @Nullable Snapshot getParent() {
        return parent;
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull Map<String, List<Entry>> getSnapshots() {
        return snapshots;
    }

    public @NotNull Snapshot assign(@NotNull DataFlowState state, @NotNull String name) {
        if (!(components.containsKey(name))) {
            throw new IllegalArgumentException("Cannot assign value to component: " + name + " in snapshot of type: " + type + "; the specified component was not found");
        }
        RapidType componentType = components.get(name);
        Snapshot snapshot = Snapshot.createSnapshot(componentType);
        snapshots.computeIfAbsent(name, key -> new ArrayList<>());
        snapshots.get(name).add(new Entry(state, snapshot));
        return snapshot;
    }

    public @NotNull Snapshot getSnapshot(@NotNull DataFlowState state, @NotNull String name) {
        if (!(components.containsKey(name))) {
            throw new IllegalArgumentException("Cannot find snapshot for component: " + name + " in snapshot of type: " + type + "; the specified component was not found");
        }
        if(snapshots.containsKey(name)) {
            List<Entry> assignments = snapshots.get(name);
            int index = getValidAssignment(state, assignments);
            if(index >= 0) {
                return assignments.get(index).snapshot();
            }
        }
        Snapshot snapshot = defaultValue.getDefaultValue(this, state, components.get(name));
        snapshots.computeIfAbsent(name, key -> new ArrayList<>());
        snapshots.get(name).add(new Entry(state, snapshot));
        return snapshot;
    }

    private int getValidAssignment(@NotNull DataFlowState state, @NotNull List<Entry> assignments) {
        for (int i = assignments.size() - 1; i >= 0; i--) {
            Entry assignment = assignments.get(i);
            if (state.isAncestor(assignment.state())) {
                return i;
            }
        }
        return -1;
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

    public record Entry(@NotNull DataFlowState state, @NotNull Snapshot snapshot) {}

    @FunctionalInterface
    public interface DefaultValueProvider {
        @NotNull Snapshot getDefaultValue(@NotNull RecordSnapshot snapshot, @NotNull DataFlowState state, @NotNull RapidType componentType);
    }

}
