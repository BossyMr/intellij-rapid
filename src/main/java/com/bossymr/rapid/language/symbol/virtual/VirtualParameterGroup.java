package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class VirtualParameterGroup implements RapidParameterGroup {

    private final @NotNull VirtualRoutine routine;
    private final boolean isOptional;
    private final @NotNull List<VirtualParameter> parameters;

    public VirtualParameterGroup(@NotNull VirtualRoutine routine, boolean isOptional, @NotNull List<VirtualParameter> parameters) {
        this.routine = routine;
        this.isOptional = isOptional;
        this.parameters = parameters;
    }

    @Override
    public @NotNull RapidRoutine getRoutine() {
        return routine;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public @NotNull List<VirtualParameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameterGroup that = (VirtualParameterGroup) o;
        return isOptional == that.isOptional && Objects.equals(routine, that.routine) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routine, isOptional, parameters);
    }

    @Override
    public String toString() {
        return "VirtualParameterGroup{" +
                "routine=" + routine +
                ", isOptional=" + isOptional +
                ", parameters=" + parameters +
                '}';
    }
}
