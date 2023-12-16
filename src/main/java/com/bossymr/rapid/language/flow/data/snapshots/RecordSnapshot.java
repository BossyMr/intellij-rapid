package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.value.Expression;
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

    private final @NotNull Map<String, Expression> snapshots;
    private final @NotNull Map<String, Expression> roots;

    public RecordSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        this.type = type;
        this.optionality = optionality;
        if (!(type.getRootStructure() instanceof RapidRecord)) {
            throw new IllegalArgumentException();
        }
        this.snapshots = new HashMap<>();
        this.roots = new HashMap<>();
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull Map<String, Expression> getSnapshots() {
        return snapshots;
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
