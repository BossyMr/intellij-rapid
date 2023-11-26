package com.bossymr.rapid.language.flow.data.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code DataFlowEdge} represents a branch of the control flow.
 */
public final class DataFlowEdge {

    private final @Nullable DataFlowBlock source;
    private final @NotNull DataFlowBlock destination;

    private DataFlowState state;

    public DataFlowEdge(@Nullable DataFlowBlock source, @NotNull DataFlowBlock destination, @NotNull DataFlowState state) {
        this.source = source;
        this.destination = destination;
        this.state = state;
    }

    public @Nullable DataFlowBlock getSource() {
        return source;
    }

    public @NotNull DataFlowBlock getDestination() {
        return destination;
    }

    public @NotNull DataFlowState getState() {
        return state;
    }

    public void setState(@NotNull DataFlowState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataFlowEdge) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.destination, that.destination) &&
                Objects.equals(this.state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, state);
    }

    @Override
    public String toString() {
        return "DataFlowEdge[" +
                "source=" + source + ", " +
                "destination=" + destination + ", " +
                "states=" + state + ']';
    }
}
