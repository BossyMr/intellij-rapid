package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.EntryInstruction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.ConditionalBranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.intellij.openapi.progress.ProgressManager;
import com.microsoft.z3.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A {@code DataFlowAnalyzer} is an analyzer which analyzes a block.
 */
public class DataFlowAnalyzer {

    private final @NotNull Block.FunctionBlock functionBlock;

    private final @NotNull Map<Instruction, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;
    private final @NotNull Deque<DataFlowState> workList;

    private final @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowState> consumer;

    public DataFlowAnalyzer(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Deque<DataFlowState> workList, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowState> consumer) {
        this.functionBlock = functionBlock;
        this.functionMap = functionMap;
        this.workList = workList;
        this.blocks = blocks;
        this.consumer = consumer;
    }

    public static @NotNull Map<Instruction, DataFlowBlock> analyze(@NotNull AtomicReference<Context> context, @NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowFunctionMap functionMap, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowState> consumer) {
        List<Instruction> instructions = functionBlock.getInstructions();
        Map<Instruction, DataFlowBlock> blocks = instructions.stream().collect(Collectors.toMap(block -> block, instruction -> new DataFlowBlock(context, instruction)));
        Deque<DataFlowState> workList = new ArrayDeque<>(instructions.size());
        for (EntryInstruction entryInstruction : functionBlock.getEntryInstructions()) {
            Instruction instruction = entryInstruction.getInstruction();
            DataFlowBlock block = blocks.get(instruction);
            DataFlowState state = DataFlowState.createState(block);
            workList.add(state);
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
        return blocks;
    }

    public static void reanalyze(@NotNull DataFlowState state, @NotNull DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull BiPredicate<Map<Instruction, DataFlowBlock>, DataFlowState> consumer) {
        Deque<DataFlowState> workList = new ArrayDeque<>();
        workList.addLast(state);
        Block.FunctionBlock functionBlock = (Block.FunctionBlock) state.getBlock().getInstruction().getBlock();
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(functionBlock, functionMap, blocks, workList, consumer);
        analyzer.process();
    }

    public @NotNull Block.FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public static @Nullable DataFlowState getPreviousCycle(@NotNull DataFlowState state) {
        DataFlowBlock dataFlowBlock = state.getBlock();
        DataFlowState predecessor = state;
        while ((predecessor = predecessor.getPredecessor()) != null) {
            if (predecessor.getBlock().getInstruction().equals(dataFlowBlock.getInstruction())) {
                return predecessor;
            }
        }
        return null;
    }

    public static int getPreviousCycles(@NotNull DataFlowState state) {
        int count = 0;
        while ((state = getPreviousCycle(state)) != null) {
            count++;
        }
        return count;
    }

    public void process() {
        while (!(workList.isEmpty())) {
            ProgressManager.checkCanceled();
            DataFlowState state = workList.removeFirst();
            List<DataFlowState> successors = process(state);
            if (consumer.test(blocks, state)) {
                for (DataFlowState successor : successors) {
                    if (getPreviousCycles(successor) >= 2) {
                        continue;
                    }
                    if (workList.contains(successor)) {
                        continue;
                    }
                    workList.add(successor);
                }
            }
        }
    }

    private @NotNull List<DataFlowState> process(@NotNull DataFlowState state) {
        cutBranch(state);
        getVolatileExpression(state).clear();
        state.getSnapshots().clear();
        state.getSnapshots().putAll(state.getRoots());
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(this, state, blocks, functionMap);
        Instruction instruction = state.getBlock().getInstruction();
        return instruction.accept(visitor);
    }

    private @NotNull List<Expression> getVolatileExpression(@NotNull DataFlowState state) {
        DataFlowState predecessor = state.getPredecessor();
        List<Expression> expressions = state.getExpressions();
        if (predecessor != null) {
            DataFlowBlock predecessorBlock = predecessor.getBlock();
            Instruction instruction = predecessorBlock.getInstruction();
            if (instruction instanceof ConditionalBranchingInstruction) {
                if (!(expressions.isEmpty())) {
                    return expressions.subList(1, expressions.size());
                }
            }
        }
        return expressions;
    }

    private void cutBranch(@NotNull DataFlowState origin) {
        Deque<DataFlowState> queue = new ArrayDeque<>(origin.getSuccessors());
        while (!(queue.isEmpty())) {
            DataFlowState successor = queue.removeLast();
            functionMap.getWorkList().removeIf(state -> state.equals(successor));
            functionMap.unregisterState(successor);
            successor.close();
            queue.addAll(successor.getSuccessors());
        }
    }
}
