package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.ValueType;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RapidAliasType implements RapidType {

    private final @NotNull Pointer<? extends RapidAlias> alias;

    public RapidAliasType(@NotNull RapidAlias alias) {
        this.alias = alias.createPointer();
    }

    public @Nullable RapidType getUnderlyingType() {
        RapidAlias structure = getStructure();
        if (structure == null) {
            return null;
        }
        return structure.getType();
    }

    @Override
    public @Nullable RapidAlias getStructure() {
        return alias.dereference();
    }

    @Override
    public @Nullable RapidStructure getRootStructure() {
        RapidType underlyingType = getUnderlyingType();
        if (underlyingType == null) {
            return getStructure();
        }
        return underlyingType.getRootStructure();
    }

    @Override
    public @NotNull ValueType getValueType() {
        RapidType underlyingType = getUnderlyingType();
        if (underlyingType == null) {
            return ValueType.UNKNOWN;
        }
        return underlyingType.getValueType();
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
        return Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}
