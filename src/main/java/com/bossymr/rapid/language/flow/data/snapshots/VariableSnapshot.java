package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code VariableSnapshot} represents the previous state of a variable. The below example shows how snapshots can be
 * used, as each variable state is represented as a snapshot. This is useful, as although {@code x} is modified, if the
 * value {@code z} and {@code y} is discovered, the value of the other is immediately discovered.
 * <pre>{@code
 *                                          // State:
 * 0: x = <some value between 0 and 10>     // x1 = 10
 * 1: z = x + 2                             // z1 = x1 + 2
 * 2: y = (x == 2)                          // y1 = x1 == 2
 * 3: x = 10                                // x2 = 10
 * }
 */
public class VariableSnapshot implements Snapshot {

    private final @Nullable Snapshot snapshot;
    private final @NotNull RapidType type;
    private final @NotNull Optionality optionality;

    public VariableSnapshot(@Nullable Snapshot snapshot, @NotNull RapidType type, @NotNull Optionality optionality) {
        this.snapshot = snapshot;
        if (type.getDimensions() > 0) {
            throw new IllegalArgumentException("Cannot create VariableSnapshot for variable of type: " + type);
        }
        if (type.getRootStructure() instanceof RapidRecord) {
            throw new IllegalArgumentException("Cannot create VariableSnapshot for variable of type: " + type);
        }
        this.type = type;
        this.optionality = optionality;
    }

    @Override
    public @Nullable Snapshot getParent() {
        return snapshot;
    }

    @Override
    public @NotNull Optionality getOptionality() {
        return optionality;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
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
