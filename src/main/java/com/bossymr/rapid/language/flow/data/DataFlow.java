package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DataFlow {

    private final @NotNull ControlFlow controlFlow;
    private final @NotNull Map<Block, DataFlowBlock> entry;

    public DataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<Block, DataFlowBlock> entry) {
        this.controlFlow = controlFlow;
        this.entry = entry;
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow;
    }

    public @NotNull DataFlowBlock getBlock(@NotNull Block block) {
        return entry.get(block);
    }
}
