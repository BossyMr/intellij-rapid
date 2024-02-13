package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidUnknownType implements RapidType {

    private final @NotNull String name;

    public RapidUnknownType(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean isRecord() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public @Nullable RapidStructure getStructure() {
        return null;
    }

    @Override
    public @Nullable RapidStructure getRootStructure() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidUnknownType that = (RapidUnknownType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}
