package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public interface Snapshot {

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type) {
        return createSnapshot(type, Optionality.PRESENT);
    }

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        if (type.getDimensions() > 0) {
            return new ArraySnapshot(type, optionality, snapshot -> {});
        }
        if (type.getRootStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(type, optionality);
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }

            }
        }
    }

    @NotNull Optionality getOptionality();

    @NotNull RapidType getType();
}
