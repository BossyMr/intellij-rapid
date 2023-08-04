package com.bossymr.rapid.language.flow.data.block;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A {@code DataFlowEdge} represents a branch of the control flow.
 */
public final class DataFlowEdge {

    private final @NotNull DataFlowBlock source;
    private final @NotNull DataFlowBlock destination;

    private final @NotNull List<DataFlowState> states;
    private List<DataFlowState> latest;

    public DataFlowEdge(@NotNull DataFlowBlock source, @NotNull DataFlowBlock destination, @NotNull List<DataFlowState> states) {
        this.source = source;
        this.destination = destination;
        this.states = states;
    }

    public @NotNull DataFlowBlock getSource() {
        return source;
    }

    public @NotNull DataFlowBlock getDestination() {
        return destination;
    }

    public @NotNull List<DataFlowState> getStates() {
        latest = states.stream()
                .map(DataFlowState::copy)
                .toList();
        return latest;
    }

    public List<DataFlowState> getLatest() {
        return latest;
    }

    public void setLatest(@NotNull List<DataFlowState> states) {
        this.latest = states;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataFlowEdge) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.destination, that.destination) &&
                Objects.equals(this.states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, states);
    }

    @Override
    public String toString() {
        return "DataFlowEdge[" +
                "source=" + source + ", " +
                "destination=" + destination + ", " +
                "states=" + states + ']';
    }
}
