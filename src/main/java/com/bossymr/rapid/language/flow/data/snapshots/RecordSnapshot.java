package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A snapshot based on a record, which itself has snapshots for each component of the record.
 */
public class RecordSnapshot implements Snapshot {

    private final @Nullable Snapshot parent;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;

    public RecordSnapshot(@Nullable Snapshot parent, @NotNull RapidType type, @NotNull Optionality optionality) {
        this.parent = parent;
        this.type = type;
        this.optionality = optionality;
    }

    @Override
    public @Nullable Snapshot getParent() {
        return parent;
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    public @NotNull RecordSnapshot copy() {
        return new RecordSnapshot(parent, type, optionality);
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
