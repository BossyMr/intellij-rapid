package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.ControlFlowListener;
import com.bossymr.rapid.language.flow.EntryInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * A {@code DataFlowAnalyzer} is an analyzer which analyzes a block.
 */
public class DataFlowAnalyzer {

    private final @NotNull ControlFlowBlock block;
    private final @NotNull Deque<DataFlowState> workList;
    private final @NotNull Set<RapidRoutine> stack;

    private DataFlowAnalyzer(@NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block, @NotNull Deque<DataFlowState> workList) {
        this.block = block;
        this.workList = workList;
        this.stack = stack;
    }

    public static void computeDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block) {
        Block controlFlow = block.getControlFlow();
        Deque<DataFlowState> workList = new ArrayDeque<>(controlFlow.getInstructions().size());
        for (EntryInstruction instruction : controlFlow.getEntryInstructions()) {
            DataFlowState state = DataFlowState.createState(block, instruction.getInstruction());
            block.getDataFlow().put(instruction.getEntryType(), state);
            workList.add(DataFlowState.createSuccessorState(instruction.getInstruction(), state));
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(stack, block, workList);
        analyzer.process();
    }

    public static @Nullable DataFlowState getPreviousCycle(@NotNull DataFlowState state) {
        Instruction instruction = state.getInstruction();
        List<DataFlowState> chain = state.getPredecessorChain();
        for (int i = 1; i < chain.size(); i++) {
            DataFlowState previousState = chain.get(i - 1);
            DataFlowState currentState = chain.get(i);
            if (previousState.getInstruction().equals(currentState.getInstruction())) {
                if (!(previousState.getInstruction().getPredecessors().contains(currentState.getInstruction()))) {
                    continue;
                }
            }
            if (currentState.getInstruction().equals(instruction)) {
                return currentState;
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
            ControlFlowListener.publish().onState(state);
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

    private @NotNull List<DataFlowState> process(@NotNull DataFlowState state) {
        List<DataFlowState> chain = state.getPredecessorChain();
        DataFlowState origin = chain.get(chain.size() - 1);
        if (!(block.getDataFlow().containsValue(origin))) {
            return List.of();
        }
        for (DataFlowState successor : state.getSuccessors()) {
            successor.prune(workList::remove);
        }
        state.clear();
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(stack, state, block);
        Instruction instruction = state.getInstruction();
        return instruction.accept(visitor);
    }
}
