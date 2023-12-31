package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public interface Snapshot {

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type) {
        return createSnapshot(type, Optionality.PRESENT);
    }

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        if (type.getDimensions() > 0) {
            RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
            return new ArraySnapshot(type, optionality, state -> createSnapshot(arrayType));
        }
        if (type.getRootStructure() instanceof RapidRecord) {
            return new RecordSnapshot(type, optionality, (state, componentType) -> createSnapshot(componentType));
        }
        return new VariableSnapshot(type, optionality);
    }

    @NotNull Optionality getOptionality();

    @NotNull RapidType getType();
}
