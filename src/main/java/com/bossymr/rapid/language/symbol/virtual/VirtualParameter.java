package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VirtualParameter(
        @NotNull RapidParameterGroup parameterGroup,
        @NotNull Attribute attribute,
        @NotNull String name,
        @NotNull RapidType type
) implements RapidParameter, VirtualSymbol {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute();
    }

    @Override
    public @NotNull RapidParameterGroup getParameterGroup() {
        return parameterGroup();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameter that = (VirtualParameter) o;
        return getAttribute() == that.getAttribute() && getName().equals(that.getName()) && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttribute(), getName(), getType());
    }

    @Override
    public String toString() {
        return "VirtualParameter{" +
                "attribute=" + attribute +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
