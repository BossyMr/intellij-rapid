package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ControlFlowBlock {

    private final @NotNull Block controlFlow;
    private final @NotNull Map<Instruction, DataFlowBlock> dataFlow;
    private final @NotNull DataFlowFunction function;

    public ControlFlowBlock(@NotNull Block controlFlow) {
        this.controlFlow = controlFlow;
        this.dataFlow = new HashMap<>();
        this.function = new DataFlowFunction(this);
    }

    public @NotNull Block getControlFlow() {
        return controlFlow;
    }

    public @NotNull DataFlowBlock getDataFlow(@NotNull Instruction instruction) {
        if (!(dataFlow.containsKey(instruction))) {
            throw new IllegalArgumentException();
        }
        return dataFlow.get(instruction);
    }

    public @NotNull Map<Instruction, DataFlowBlock> getDataFlow() {
        return dataFlow;
    }

    public @NotNull DataFlowFunction getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlFlowBlock that = (ControlFlowBlock) o;
        return Objects.equals(controlFlow, that.controlFlow) && Objects.equals(dataFlow, that.dataFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controlFlow, dataFlow);
    }

    @Override
    public String toString() {
        return "ControlFlowBlock{" +
                "block=" + controlFlow.getModuleName() + ":" + controlFlow.getName() +
                '}';
    }
}
