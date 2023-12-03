package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidArrayType implements RapidType {

    private final @NotNull RapidType underlyingType;

    public RapidArrayType(@NotNull RapidType underlyingType) {
        this.underlyingType = underlyingType;
    }

    public @NotNull RapidType getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public @NotNull RapidType createArrayType(int dimensions) {
        return getUnderlyingType().createArrayType(dimensions);
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
    public @Nullable RapidStructure getRootStructure() {
        return underlyingType.getRootStructure();
    }

    @Override
    public @NotNull ValueType getValueType() {
        return underlyingType.getValueType();
    }

    @Override
    public @NotNull String getText() {
        return underlyingType.getText();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        RapidArrayType that = (RapidArrayType) object;
        return Objects.equals(underlyingType, that.underlyingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(underlyingType);
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}
