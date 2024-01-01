package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Snapshot {

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        return createSnapshot(null, type, optionality);
    }

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type) {
        return createSnapshot(type, Optionality.PRESENT);
    }

    static @NotNull Snapshot createSnapshot(@Nullable Snapshot parent, @NotNull RapidType type, @NotNull Optionality optionality) {
        if (type.getDimensions() > 0) {
            RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
            return new ArraySnapshot(parent, type, optionality, (current, state) -> createSnapshot(current, arrayType, Optionality.PRESENT));
        }
        if (type.getRootStructure() instanceof RapidRecord) {
            return new RecordSnapshot(parent, type, optionality, (current, state, componentType) -> createSnapshot(current, componentType, Optionality.PRESENT));
        }
        return new VariableSnapshot(parent, type, optionality);
    }

    @Nullable Snapshot getParent();

    @NotNull Optionality getOptionality();

    @NotNull RapidType getType();
}
