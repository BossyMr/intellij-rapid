package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.EntryInstruction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowEdge;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.ConditionalBranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantExpression;
import com.bossymr.rapid.language.flow.value.Expression;
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

    private final @NotNull Set<BlockCycle> cycles;
    private final @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Deque<DataFlowBlock> workList, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.workList = workList;
        this.blocks = blocks;
        this.consumer = consumer;
        this.cycles = getBlockCycles(functionBlock);
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

    private static @NotNull Set<BlockCycle> getBlockCycles(@NotNull Block block) {
        Set<BlockCycle> blockCycles = new HashSet<>();
        Set<List<Instruction>> cycles = getCycles(block);
        for (List<Instruction> cycle : cycles) {
            BlockCycle blockCycle = new BlockCycle(cycle, new HashMap<>(), new HashMap<>());
            blockCycles.add(blockCycle);
            for (Instruction instruction : cycle) {
                if (instruction instanceof ConditionalBranchingInstruction branchingInstruction) {
                    Expression condition = branchingInstruction.getCondition();
                    int nextIndex = cycle.indexOf(instruction) + 1;
                    Instruction nextInstruction = cycle.size() > nextIndex ? cycle.get(nextIndex) : cycle.get(0);
                    if (branchingInstruction.getTrue() != null && branchingInstruction.getTrue().equals(nextInstruction)) {
                        blockCycle.guards().put(branchingInstruction, new BinaryExpression(BinaryOperator.EQUAL_TO, condition, new ConstantExpression(true)));
                        if (branchingInstruction.getFalse() != null) {
                            blockCycle.exits().put(instruction, branchingInstruction.getFalse());
                        }
                    }
                    if (branchingInstruction.getFalse() != null && branchingInstruction.getFalse().equals(nextInstruction)) {
                        blockCycle.guards().put(branchingInstruction, new BinaryExpression(BinaryOperator.EQUAL_TO, condition, new ConstantExpression(false)));
                        if (branchingInstruction.getTrue() != null) {
                            blockCycle.exits().put(instruction, branchingInstruction.getTrue());
                        }
                    }
                }
            }
        }
        return blockCycles;
    }

    private static @NotNull Set<List<Instruction>> getCycles(@NotNull Block block) {
        Set<List<Instruction>> cycles = new HashSet<>();
        Map<Instruction, Set<List<Instruction>>> tails = new HashMap<>();
        Deque<Instruction> queue = new ArrayDeque<>();
        for (EntryInstruction entryInstruction : block.getEntryInstructions()) {
            queue.add(entryInstruction.getInstruction());
        }
        while (!(queue.isEmpty())) {
            Instruction instruction = queue.removeFirst();
            // If this an entry instruction.
            tails.computeIfAbsent(instruction, unused -> new HashSet<>());
            for (Instruction successor : instruction.getSuccessors()) {
                tails.computeIfAbsent(successor, unused -> new HashSet<>());
                if (tails.get(instruction).isEmpty()) {
                    // If this an entry instruction.
                    tails.get(successor).add(List.of(instruction));
                } else {
                    for (List<Instruction> sequence : tails.get(instruction)) {
                        List<Instruction> copy = new ArrayList<>(sequence);
                        copy.add(instruction);
                        tails.get(successor).add(copy);
                    }
                }
                List<List<Instruction>> deleteQueue = new ArrayList<>();
                for (List<Instruction> sequence : tails.get(successor)) {
                    if (sequence.contains(successor)) {
                        deleteQueue.add(sequence);
                        cycles.add(sequence.subList(sequence.indexOf(successor), sequence.size()));
                    }
                }
                deleteQueue.forEach(tails.get(successor)::remove);
                if (deleteQueue.isEmpty()) {
                    queue.add(successor);
                }
            }
        }
        return cycles;
    }

    public Block.@NotNull FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public @NotNull Map<Instruction, Set<DataFlowState>> processCycle(@NotNull DataFlowBlock predecessorBlock, @NotNull DataFlowState successorState, @NotNull BlockCycle blockCycle) {
        Map<Instruction, Set<DataFlowState>> exits = new HashMap<>();
        Deque<DataFlowBlock> workList = new ArrayDeque<>();
        DataFlowBlock firstBlock = blocks.get(blockCycle.instructions().get(0));
        workList.add(firstBlock);
        while (!(workList.isEmpty())) {
            DataFlowBlock block = workList.removeLast();
            Instruction instruction = block.getInstruction();
            process(block);
            if(blockCycle.exits().containsValue(instruction)) {
                exits.computeIfAbsent(instruction, key -> new HashSet<>());
                exits.get(instruction).addAll(block.getStates());
            } else {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    DataFlowBlock successorBlock = successor.getDestination();
                    if (workList.contains(successorBlock)) {
                        continue;
                    }
                    workList.add(successorBlock);
                }
            }
        }
        return exits;
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
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(this, block, blocks, cycles, functionMap);
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
