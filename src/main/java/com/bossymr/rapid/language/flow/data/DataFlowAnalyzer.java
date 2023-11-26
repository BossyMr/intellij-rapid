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
import org.jetbrains.annotations.Unmodifiable;

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

    public static @NotNull Map<Instruction, DataFlowBlock> analyze(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Set<BlockCycle> cycles, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        List<Instruction> instructions = functionBlock.getInstructions();
        Map<Instruction, DataFlowBlock> blocks = instructions.stream().collect(Collectors.toMap(block -> block, instruction -> new DataFlowBlock(instruction, cycles)));
        Deque<DataFlowBlock> workList = new ArrayDeque<>(instructions.size());
        for (Instruction basicBlock : instructions) {
            workList.add(blocks.get(basicBlock));
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
        return blocks;
    }

    public static void reanalyze(@NotNull DataFlowBlock block, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowBlock> consumer) {
        Deque<DataFlowBlock> workList = new ArrayDeque<>();
        workList.addLast(block);
        Block.FunctionBlock functionBlock = (Block.FunctionBlock) block.getInstruction().getBlock();
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
    }

    public static @NotNull Set<BlockCycle> getBlockCycles(@NotNull Block block) {
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
        System.out.println(blockCycles);
        return blockCycles;
    }

    private static @NotNull @Unmodifiable Set<List<Instruction>> getCycles(@NotNull Block block) {
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
        return Set.copyOf(cycles);
    }

    public Block.@NotNull FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public void process() {
        while (!(workList.isEmpty())) {
            ProgressManager.checkCanceled();
            DataFlowBlock block = workList.removeFirst();
            Set<DataFlowEdge> successors = Set.copyOf(block.getSuccessors());
            process(block);
            if (consumer.test(blocks, block)) {
                for (DataFlowEdge successor : block.getSuccessors()) {
                    if (successors.contains(successor)) {
                        continue;
                    }
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
        Instruction instruction = block.getInstruction();

        if (block.getPredecessors().isEmpty()) {
            /*
             * This block does not have any predecessor. Add a new state with all variables initialized.
             */
            block.getStates().clear();
            Collection<EntryInstruction> entries = instruction.getBlock().getEntryInstructions();
            if (entries.stream().anyMatch(entry -> entry.getInstruction().equals(instruction))) {
                DataFlowEdge edge = new DataFlowEdge(null, block, DataFlowState.createState(block));
                block.getPredecessors().add(edge);
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

        /*
         * Find all new predecessors by checking whether a state exists, which is a successor to the predecessor.
         */
        for (DataFlowEdge predecessor : block.getPredecessors()) {
            DataFlowState predecessorState = predecessor.getState();
            if (block.getStates().stream().noneMatch(state -> state.isAncestor(predecessorState))) {
                /*
                 * There exists no state in the block which is a successor to this predecessor.
                 * As a result, it must be a new predecessor.
                 */
                block.getStates().add(predecessorState);
                DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(this, block, predecessorState, blocks, functionMap);
                instruction.accept(visitor);
            }
        }

        /*
         * Find all removed predecessors by checking whether a predecessor exists for each state.
         */
        for (ListIterator<DataFlowState> iterator = block.getStates().listIterator(); iterator.hasNext(); ) {
            DataFlowState state = iterator.next();
            if (block.getPredecessors().stream().noneMatch(predecessor -> state.isAncestor(predecessor.getState()))) {
                /*
                 * There exists no predecessor which this state is an ancestor of.
                 * As a result, it must have been removed.
                 */
                iterator.remove();
                block.getSuccessors().removeIf(successor -> {
                    // This successor is an ancestor to this state.
                    boolean isAncestor = successor.getState().isAncestor(state);
                    if (isAncestor) {
                        successor.getDestination().getPredecessors().remove(successor);
                    }
                    return isAncestor;
                });
            }
        }
    }
}
