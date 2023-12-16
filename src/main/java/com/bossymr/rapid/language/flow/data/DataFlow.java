package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class DataFlow {

    private final @NotNull Map<Instruction, DataFlowBlock> map;
    private final @NotNull ControlFlow controlFlow;
    private final @NotNull Map<DataFlowState, DataFlowUsage> usages;

    public DataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Map<DataFlowState, DataFlowUsage> usages) {
        this.map = Map.copyOf(blocks);
        this.controlFlow = controlFlow;
        this.usages = usages;
    }

    public @NotNull Collection<DataFlowBlock> getBlocks() {
        return map.values();
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow;
    }

    public @NotNull Map<DataFlowState, DataFlowUsage> getUsages() {
        return usages;
    }

    public @Nullable DataFlowBlock getBlock(@NotNull Instruction block) {
        return map.get(block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlow dataFlow = (DataFlow) o;
        return Objects.equals(map, dataFlow.map) && Objects.equals(controlFlow, dataFlow.controlFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, controlFlow);
    }

    @Override
    public String toString() {
        return "DataFlow{" + map + '}';
    }
}
