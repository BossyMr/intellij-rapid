package com.bossymr.rapid.language.flow.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public final class DataFlowEdge {

    private final @NotNull DataFlowBlock block;
    private final @NotNull Set<DataFlowState> states;

    public DataFlowEdge(@NotNull DataFlowBlock block, @NotNull Set<DataFlowState> states) {
        this.block = block;
        this.states = states;
    }

    public @NotNull DataFlowBlock getBlock() {
        return block;
    }

    public @NotNull Set<DataFlowState> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlowEdge edge = (DataFlowEdge) o;
        return Objects.equals(block, edge.block) && Objects.equals(states, edge.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, states);
    }

    @Override
    public String toString() {
        return "DataFlowEdge{" +
                "block=" + block +
                ", states=" + states +
                '}';
    }
}
