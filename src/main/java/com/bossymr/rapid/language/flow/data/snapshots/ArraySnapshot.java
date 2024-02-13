package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A snapshot which represents an array assignment.
 */
public class ArraySnapshot implements Snapshot {

    private final @Nullable Snapshot parent;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;
    private final boolean hasIdentity;

    public ArraySnapshot(@Nullable Snapshot parent, @NotNull RapidType type, @NotNull Optionality optionality, boolean hasIdentity) {
        this.hasIdentity = hasIdentity;
        if (!(type.isArray())) {
            throw new IllegalArgumentException("Cannot create array snapshot for variable of type: " + type);
        }
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

    public @NotNull ArraySnapshot copy() {
        return new ArraySnapshot(parent, type, optionality, hasIdentity);
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean hasIdentity() {
        return hasIdentity;
    }

    @Override
    public boolean equals(Object object) {
        return hasIdentity && super.equals(object);
    }

    @Override
    public String toString() {
        return "~" + hashCode() + (hasIdentity ? "" : "I") + "[" + switch (getOptionality()) {
            case PRESENT -> "P";
            case UNKNOWN -> "P/M";
            case MISSING -> "M";
            case NO_VALUE -> "";
        } + "]";
    }
}
