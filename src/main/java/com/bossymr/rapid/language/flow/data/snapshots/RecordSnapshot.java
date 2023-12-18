package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements Snapshot {

    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;

    private final @NotNull Map<String, Snapshot> snapshots;
    private final @NotNull Map<String, Snapshot> roots;

    public RecordSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        this.type = type;
        this.optionality = optionality;
        if (!(type.getRootStructure() instanceof RapidRecord
        )) {
            throw new IllegalArgumentException();
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

    public @NotNull Snapshot getSnapshot(@NotNull String name) {
        if (!(snapshots.containsKey(name))) {
            throw new IllegalArgumentException();
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
