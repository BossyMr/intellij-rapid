package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VirtualParameter implements RapidParameter, VirtualSymbol {

    private final @NotNull VirtualParameterGroup parameterGroup;
    private final @NotNull ParameterType parameterType;
    private final @NotNull String name;
    private final @NotNull RapidType type;

    public VirtualParameter(@NotNull VirtualParameterGroup parameterGroup, @NotNull ParameterType parameterType, @NotNull String name, @NotNull RapidType type) {
        this.parameterGroup = parameterGroup;
        this.parameterType = parameterType;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull ParameterType getParameterType() {
        return parameterType;
    }

    @Override
    public @NotNull VirtualParameterGroup getParameterGroup() {
        return parameterGroup;
    }

    @Override
    public @NotNull String getCanonicalName() {
        return RapidParameter.super.getCanonicalName();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull VirtualPointer<VirtualParameter> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameter that = (VirtualParameter) o;
        return parameterType == that.parameterType && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterType, name, type);
    }

    @Override
    public String toString() {
        return "VirtualParameter{" +
                "parameterType=" + parameterType +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
