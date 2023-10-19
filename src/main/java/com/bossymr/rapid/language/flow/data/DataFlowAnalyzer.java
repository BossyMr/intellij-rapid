package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.EntryInstruction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A {@code DataFlowAnalyzer} is an analyzer which analyzes a block.
 */
public class DataFlowAnalyzer {

    private final @NotNull Block.FunctionBlock functionBlock;

    private final @NotNull Map<Instruction, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;
    private final @NotNull Deque<DataFlowBlock> workList;

    private final @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Deque<DataFlowBlock> workList, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.workList = workList;
        this.blocks = blocks;
        this.consumer = consumer;
    }

    public static @NotNull Map<Instruction, DataFlowBlock> analyze(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        List<Instruction> instructions = functionBlock.getInstructions();
        Map<Instruction, DataFlowBlock> blocks = instructions.stream().collect(Collectors.toMap(block -> block, DataFlowBlock::new));
        Deque<DataFlowBlock> workList = new ArrayDeque<>(instructions.size());
        for (Instruction basicBlock : instructions) {
            workList.add(blocks.get(basicBlock));
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
        return blocks;
    }

    public static void reanalyze(@NotNull DataFlowBlock block, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        Deque<DataFlowBlock> deque = new ArrayDeque<>();
        deque.addLast(block);
        Block.FunctionBlock functionBlock = (Block.FunctionBlock) block.getInstruction().getBlock();
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, deque, consumer);
        analyzer.process();
    }

    public void process() {
        while (!(workList.isEmpty())) {
            ProgressManager.checkCanceled();
            DataFlowBlock block = workList.removeFirst();
            process(block);
            if (consumer.test(blocks, block)) {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    DataFlowBlock successorBlock = successor.getDestination();
                    if (workList.contains(successorBlock)) {
                        continue;
                    }
                    workList.add(successorBlock);
                }
            }
        }
    }

    private void process(@NotNull DataFlowBlock block) {
        block.getStates().clear();
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getDestination().getPredecessors().remove(successor);
        }
        block.getSuccessors().clear();
        for (DataFlowEdge predecessors : block.getPredecessors()) {
            block.getStates().add(predecessors.getState());
        }
        Instruction instruction = block.getInstruction();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        if (block.getStates().isEmpty()) {
            Collection<EntryInstruction> entries = instruction.getBlock().getEntryInstructions();
            if (entries.stream().anyMatch(entry -> entry.getInstruction().equals(instruction))) {
                block.getStates().add(DataFlowState.createState(block));
            } else {
                /*
                 * This block has no predecessors and is not the entry point of a function, as such, assume that any
                 * variable might be equal to any value. Do not continue analyzing this block; otherwise, the state of
                 * this block would extend into other blocks, which are reachable.
                 */
                block.getStates().add(DataFlowState.createUnknownState(block));
                return;
            }
        }
        instruction.accept(visitor);
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getDestination().getPredecessors().add(successor);
        }
    }
}
