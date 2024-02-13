package com.bossymr.rapid.language.flow;

import com.bossymr.network.MultiMap;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ControlFlowBlock {

    private final @NotNull Block controlFlow;
    private final @NotNull Map<BlockType, DataFlowState> dataFlow;
    private final @NotNull DataFlowFunction function;

    public ControlFlowBlock(@NotNull Block controlFlow) {
        this(controlFlow, DataFlowFunction::new);
    }

    public ControlFlowBlock(@NotNull Block controlFlow, @NotNull Function<ControlFlowBlock, DataFlowFunction> function) {
        this.controlFlow = controlFlow;
        this.dataFlow = new MultiMap<>();
        this.function = function.apply(this);
    }

    public @NotNull Block getControlFlow() {
        return controlFlow;
    }

    public @NotNull Set<DataFlowState> getDataFlow(@NotNull Instruction instruction) {
        if (!(instruction.getBlock().equals(controlFlow))) {
            throw new IllegalArgumentException("Could not find instruction: " + instruction + " in block: " + controlFlow);
        }
        BlockType blockType = getBlockType(instruction);
        Set<DataFlowState> result = new HashSet<>();
        Deque<DataFlowState> queue = new ArrayDeque<>();
        queue.add(dataFlow.get(blockType));
        while (!(queue.isEmpty())) {
            DataFlowState state = queue.removeLast();
            if (state.getInstruction().equals(instruction)) {
                result.add(state);
            }
            queue.addAll(state.getSuccessors());
        }
        return result;
    }

    public int getSize() {
        int size = 0;
        Deque<DataFlowState> queue = new ArrayDeque<>(dataFlow.values());
        while (!(queue.isEmpty())) {
            DataFlowState state = queue.removeLast();
            size += 1;
            queue.addAll(state.getSuccessors());
        }
        return size;
    }

    private @NotNull BlockType getBlockType(@NotNull Instruction instruction) {
        int index = instruction.getIndex();
        BlockType blockType = null;
        List<EntryInstruction> entryInstructions = new ArrayList<>(controlFlow.getEntryInstructions());
        entryInstructions.sort(Comparator.comparing(entryInstruction -> entryInstruction.getInstruction().getIndex()));
        for (EntryInstruction entryInstruction : entryInstructions) {
            if (entryInstruction.getInstruction().getIndex() > index) {
                return Objects.requireNonNull(blockType);
            }
            blockType = entryInstruction.getEntryType();
        }
        return Objects.requireNonNull(blockType);
    }

    public @NotNull Map<BlockType, DataFlowState> getDataFlow() {
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
