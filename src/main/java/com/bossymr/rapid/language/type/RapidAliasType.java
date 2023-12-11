package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
    public @Nullable RapidStructure getRootStructure() {
        RapidType underlyingType = getUnderlyingType();
        if (underlyingType != null) {
            return underlyingType.getRootStructure();
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
        if (underlyingType != null) {
            return underlyingType.getText();
        } else {
            return RapidSymbol.getDefaultText();
        }
    }

    @Override
    public @NotNull RapidType createArrayType(int dimensions) {
        RapidType underlyingType = getUnderlyingType();
        if (underlyingType != null) {
            return underlyingType.createArrayType(dimensions);
        }
        return RapidType.super.createArrayType(dimensions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAliasType that = (RapidAliasType) o;
        return Objects.equals(alias.getName(), that.alias.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias.getName());
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}
