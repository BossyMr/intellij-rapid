package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements Snapshot {

    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;
    private final @NotNull Map<String, RapidType> components;
    private final @NotNull BiFunction<DataFlowState, RapidType, Snapshot> defaultValue;
    private final @NotNull Map<String, Snapshot> snapshots;
    private final @NotNull Map<String, Snapshot> roots;

    public RecordSnapshot(@NotNull RapidType type, @NotNull Optionality optionality, @NotNull BiFunction<DataFlowState, RapidType, Snapshot> defaultValue) {
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
        this.roots = new HashMap<>();
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull Map<String, Snapshot> getSnapshots() {
        return snapshots;
    }

    public void assign(@NotNull String name, @NotNull Snapshot snapshot) {
        snapshots.put(name, snapshot);
        if (!(roots.containsKey(name))) {
            roots.put(name, snapshot);
        }
    }

    public @NotNull Snapshot getSnapshot(@NotNull DataFlowState state, @NotNull String name) {
        if (!(snapshots.containsKey(name))) {
            if (!(components.containsKey(name))) {
                throw new IllegalArgumentException("Cannot create snapshot for component: " + name + " for record of type: " + type);
            }
            Snapshot snapshot = defaultValue.apply(state, components.get(name));
            snapshots.put(name, snapshot);
            return snapshot;
        }
        return snapshots.get(name);
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
}
