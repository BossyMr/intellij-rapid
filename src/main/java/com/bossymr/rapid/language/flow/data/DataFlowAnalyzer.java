package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.ControlFlowListener;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.ConditionalBranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
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

    public DataFlowAnalyzer(@NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block, @NotNull Deque<DataFlowState> workList) {
        this.block = block;
        this.workList = workList;
        this.stack = stack;
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
        cutBranch(state);
        getVolatileExpression(state).clear();
        state.getSnapshots().clear();
        state.getSnapshots().putAll(state.getRoots());
        DataFlowAnalyzerVisitor visitor = new DataFlowAnalyzerVisitor(stack, state, block);
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
            workList.removeIf(state -> state.equals(successor));
            block.getFunction().unregisterOutput(successor);
            successor.close();
            queue.addAll(successor.getSuccessors());
        }
    }
}
