package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidUnknownType implements RapidType {

    private final @NotNull String name;

    public RapidUnknownType(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @Nullable RapidStructure getStructure() {
        return null;
    }

    @Override
    public @Nullable RapidStructure getActualStructure() {
        return null;
    }

    @Override
    public @NotNull ValueType getValueType() {
        return ValueType.UNKNOWN;
    }

    @Override
    public @NotNull String getText() {
        return name;
    }
}
