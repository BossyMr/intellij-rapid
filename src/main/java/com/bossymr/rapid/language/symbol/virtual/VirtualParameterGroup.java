package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record VirtualParameterGroup(
        @NotNull VirtualRoutine routine,
        boolean isOptional,
        @NotNull List<VirtualParameter> parameters
) implements RapidParameterGroup {

    @Override
    public @NotNull RapidRoutine getRoutine() {
        return routine();
    }

    @Override
    public @NotNull List<VirtualParameter> getParameters() {
        return parameters();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameterGroup that = (VirtualParameterGroup) o;
        return isOptional() == that.isOptional() && getParameters().equals(that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOptional(), getParameters());
    }

    @Override
    public String toString() {
        return "VirtualParameterGroup{" +
                "isOptional=" + isOptional +
                ", parameters=" + parameters +
                '}';
    }
}
