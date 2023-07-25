package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class DataFlow {

    private final @NotNull Map<BasicBlock, DataFlowBlock> map;
    private final @NotNull ControlFlow controlFlow;

    public DataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<BasicBlock, DataFlowBlock> map) {
        this.map = Map.copyOf(map);
        this.controlFlow = controlFlow;
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow;
    }

    public @NotNull DataFlowBlock getBlock(@NotNull BasicBlock block) {
        if (!(map.containsKey(block))) {
            Block parentBlock = block.getBlock();
            if (parentBlock.getModuleName() == null) {
                throw new IllegalArgumentException();
            }
            Block result = controlFlow.getBlock(parentBlock.getModuleName(), parentBlock.getName());
            if (result != parentBlock) {
                throw new IllegalArgumentException();
            }
            throw new AssertionError();
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
