package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class VirtualParameterGroup implements RapidParameterGroup {

    private final boolean isOptional;
    private final List<RapidParameter> parameters;

    public VirtualParameterGroup(boolean isOptional, @NotNull List<RapidParameter> parameters) {
        this.isOptional = isOptional;
        this.parameters = parameters;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public @NotNull List<RapidParameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameterGroup that = (VirtualParameterGroup) o;
        return isOptional() == that.isOptional() && Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOptional(), getParameters());
    }

    @Override
    public String toString() {
        return "VirtualParameterGroup";
    }
}
