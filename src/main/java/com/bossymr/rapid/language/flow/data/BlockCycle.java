package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockCycle {

    private final @NotNull List<DataFlowBlock> sequence;
    private final @NotNull Set<PathCounter> pathCounters;

    public BlockCycle(@NotNull List<DataFlowBlock> sequence) {
        this.sequence = sequence;
        this.pathCounters = new HashSet<>();
    }

    public @NotNull List<DataFlowBlock> getSequence() {
        return sequence;
    }

    public @NotNull Set<PathCounter> getPathCounters() {
        return pathCounters;
    }

    public boolean hasExecuted(@NotNull List<DataFlowBlock> blockStack) {
        for (int i = blockStack.size() - 1; i >= 0; i--) {
            int j = i - (blockStack.size() - sequence.size());
            if (j < 0) {
                return true;
            }
            if (!(blockStack.get(i).equals(sequence.get(j)))) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BlockCycle blockCycle = (BlockCycle) object;
        return Objects.equals(sequence, blockCycle.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence);
    }

    @Override
    public String toString() {
        String sequenceText = sequence.stream().map(block -> {
            BasicBlock basicBlock = block.getBasicBlock();
            return String.valueOf(basicBlock.getIndex());
        }).collect(Collectors.joining(","));
        return "BlockCycle{" +
                sequenceText +
                '}';
    }
}
