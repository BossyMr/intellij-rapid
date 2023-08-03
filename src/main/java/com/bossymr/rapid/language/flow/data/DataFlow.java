package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class DataFlow {

    private final @NotNull Map<BasicBlock, DataFlowBlock> map;
    private final @NotNull ControlFlow controlFlow;
    private final @NotNull Map<DataFlowBlock, DataFlowUsage> usages;

    public DataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull Map<DataFlowBlock, DataFlowUsage> usages) {
        this.map = Map.copyOf(blocks);
        this.controlFlow = controlFlow;
        this.usages = usages;
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow;
    }

    public @NotNull Map<DataFlowBlock, DataFlowUsage> getUsages() {
        return usages;
    }

    public @NotNull DataFlowBlock getBlock(@NotNull BasicBlock block) {
        if (!(map.containsKey(block))) {
            throw new IllegalArgumentException("Could not find block: " + block);
        }
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
