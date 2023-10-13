package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantExpression;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A {@code DataFlowAnalyzer} is an analyzer which analyzes a block.
 */
public class DataFlowAnalyzer {

    private final @NotNull Block.FunctionBlock functionBlock;

    private final @NotNull Map<BasicBlock, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;
    private final @NotNull Deque<DataFlowBlock> workList;

    private final @NotNull BiPredicate<Map<BasicBlock, DataFlowBlock>, DataFlowBlock> consumer;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull Deque<DataFlowBlock> workList, @NotNull BiPredicate<Map<BasicBlock, DataFlowBlock>, DataFlowBlock> consumer) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.workList = workList;
        this.blocks = blocks;
        this.consumer = consumer;
    }

    public static @NotNull Map<BasicBlock, DataFlowBlock> analyze(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull BiPredicate<Map<BasicBlock, DataFlowBlock>, DataFlowBlock> consumer) {
        List<BasicBlock> basicBlocks = functionBlock.getInstructions();
        Set<List<BasicBlock>> cycles = getCycles(functionBlock);
        Map<BasicBlock, DataFlowBlock> blocks = basicBlocks.stream().collect(Collectors.toMap(block -> block, block -> new DataFlowBlock(block, new HashSet<>())));
        for (List<BasicBlock> cycle : cycles) {
            BlockCycle blockCycle = new BlockCycle(cycle.stream()
                    .map(blocks::get)
                    .toList());
            for (BasicBlock basicBlock : cycle) {
                DataFlowBlock block = blocks.get(basicBlock);
                if (block == null) {
                    continue;
                }
                block.getCycles().add(blockCycle);
            }
        }
        Deque<DataFlowBlock> workList = new ArrayDeque<>(basicBlocks.size());
        for (BasicBlock basicBlock : basicBlocks) {
            workList.add(blocks.get(basicBlock));
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
        return blocks;
    }

    private static @NotNull @Unmodifiable Set<List<BasicBlock>> getCycles(@NotNull Block.FunctionBlock functionBlock) {
        Set<List<BasicBlock>> blockPaths = new HashSet<>();
        Map<BasicBlock, List<BasicBlock>> trails = new HashMap<>();
        Deque<BasicBlock> queue = new ArrayDeque<>(functionBlock.getEntryBlocks());
        queue:
        while (!(queue.isEmpty())) {
            BasicBlock basicBlock = queue.removeFirst();
            List<BasicBlock> trail = trails.getOrDefault(basicBlock, List.of(basicBlock));
            trails.remove(basicBlock);
            for (BasicBlock successor : getSuccessors(basicBlock)) {
                if (trail.contains(successor)) {
                    List<BasicBlock> actual = trail.subList(trail.indexOf(successor), trail.size());
                    blockPaths.add(List.copyOf(actual));
                    continue queue;
                }
                List<BasicBlock> copy = new ArrayList<>(trail);
                copy.add(successor);
                trails.put(successor, copy);
                queue.addLast(successor);
            }
        }
        return Set.copyOf(blockPaths);
    }

    private static @NotNull Set<BasicBlock> getSuccessors(@NotNull BasicBlock block) {
        BranchingInstruction terminator = block.getTerminator();
        if (terminator instanceof BranchingInstruction.ConditionalBranchingInstruction instruction) {
            return Set.of(instruction.onFailure(), instruction.onSuccess());
        }
        if (terminator instanceof BranchingInstruction.UnconditionalBranchingInstruction instruction) {
            return Set.of(instruction.next());
        }
        if (terminator instanceof BranchingInstruction.ErrorInstruction instruction) {
            if (instruction.next() == null) {
                return Set.of();
            }
            return Set.of(instruction.next());
        }
        if (terminator instanceof BranchingInstruction.CallInstruction instruction) {
            return Set.of(instruction.next());
        }
        return Set.of();
    }

    public static void reanalyze(@NotNull DataFlowBlock block, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull BiPredicate<Map<BasicBlock, DataFlowBlock>, DataFlowBlock> consumer) {
        Deque<DataFlowBlock> deque = new ArrayDeque<>();
        deque.addLast(block);
        Block.FunctionBlock functionBlock = (Block.FunctionBlock) block.getBasicBlock().getBlock();
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
        BasicBlock basicBlock = block.getBasicBlock();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(functionBlock, block, blocks, functionMap);
        if (block.getStates().isEmpty()) {
            if (block.getBasicBlock() instanceof BasicBlock.IntermediateBasicBlock) {
                /*
                 * This block has no predecessors and is not the entry point of a function, as such, assume that any
                 * variable might be equal to any value.
                 */
                block.getStates().add(DataFlowState.createUnknownState(block));
                return;
            } else {
                block.getStates().add(DataFlowState.createState(block));
            }
        }
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(visitor);
        }
        for (DataFlowState state : block.getStates()) {
            for (PathCounter pathCounter : state.getPathCounters()) {
                for (BlockCycle blockCycle : pathCounter.getResetPath()) {
                    if (blockCycle.getSequence().contains(block)) {
                        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, pathCounter, new ConstantExpression(0)));
                        break;
                    }
                }
                for (BlockCycle blockCycle : pathCounter.getIncrementPath()) {
                    if (blockCycle.getSequence().contains(block)) {
                        BinaryExpression binaryExpression = new BinaryExpression(BinaryOperator.ADD, pathCounter, new ConstantExpression(1));
                        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, pathCounter, binaryExpression));
                        break;
                    }
                }
            }
        }
        BranchingInstruction terminator = basicBlock.getTerminator();
        terminator.accept(visitor);
        for (DataFlowEdge successor : block.getSuccessors()) {
            successor.getDestination().getPredecessors().add(successor);
        }
    }
}
