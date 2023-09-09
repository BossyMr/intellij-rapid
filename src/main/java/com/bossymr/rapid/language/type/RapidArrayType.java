package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidArrayType implements RapidType {

    private final @NotNull RapidType underlyingType;

    public RapidArrayType(@NotNull RapidType underlyingType) {
        this.underlyingType = underlyingType;
    }

    public @NotNull RapidType getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public int getDimensions() {
        if(underlyingType instanceof RapidArrayType arrayType) {
            return arrayType.getDimensions() + 1;
        } else {
            return 1;
        }
    }

    @Override
    public @Nullable RapidStructure getStructure() {
        return underlyingType.getStructure();
    }

    @Override
    public @Nullable RapidStructure getActualStructure() {
        return underlyingType.getActualStructure();
    }

    @Override
    public @NotNull ValueType getValueType() {
        return underlyingType.getValueType();
    }

    @Override
    public @NotNull String getText() {
        return underlyingType.getText();
    }
}
