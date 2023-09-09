package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAliasType implements RapidType {

    private final @NotNull RapidAlias alias;

    public RapidAliasType(@NotNull RapidAlias alias) {
        this.alias = alias;
    }

    public @Nullable RapidType getUnderlyingType() {
        return alias.getType();
    }

    @Override
    public @NotNull RapidAlias getStructure() {
        return alias;
    }

    @Override
    public @Nullable RapidStructure getActualStructure() {
        RapidType underlyingType = getUnderlyingType();
        if(underlyingType != null) {
            return underlyingType.getActualStructure();
        } else {
            return getStructure();
        }
    }

    @Override
    public @NotNull ValueType getValueType() {
        RapidType underlyingType = getUnderlyingType();
        if (underlyingType != null) {
            return underlyingType.getValueType();
        } else {
            return ValueType.UNKNOWN;
        }
    }

    @Override
    public @NotNull String getText() {
        RapidType underlyingType = getUnderlyingType();
        if(underlyingType != null) {
            return underlyingType.getText();
        } else {
            return RapidType.getDefaultText();
        }
    }
}
