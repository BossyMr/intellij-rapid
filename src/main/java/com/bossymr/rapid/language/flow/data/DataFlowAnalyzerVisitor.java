package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor<List<DataFlowState>> {

    private final @NotNull DataFlowState state;
    private final @NotNull Set<RapidRoutine> stack;
    private final @NotNull ControlFlowBlock block;

    public DataFlowAnalyzerVisitor(@NotNull Set<RapidRoutine> stack, @NotNull DataFlowState state, @NotNull ControlFlowBlock block) {
        this.state = state;
        this.stack = stack;
        this.block = block;
    }

    @Override
    public @NotNull List<DataFlowState> visitAssignmentInstruction(@NotNull AssignmentInstruction instruction) {
        DataFlowState previousCycle = DataFlowAnalyzer.getPreviousCycle(state);
        Expression expression = instruction.getExpression();
        if (previousCycle != null) {
            /*
             * If this block is in a loop (i.e. a state with this instruction is a predecessor to this state) and
             * this assignment is cyclic in nature: this assignment has the previous snapshot (or a snapshot
             * which is newer than the root snapshot of the previous iteration) of the variable which is being
             * assigned in the expression. For example, x2 := x1 + 1 or x3 := x1 + 1 (if x1 is the latest snapshot
             * for x at that point).
             */
            Snapshot snapshot = Snapshot.createSnapshot(expression.getType());
            state.assign(instruction.getVariable(), new SnapshotExpression(snapshot, expression));
        } else {
            state.assign(instruction.getVariable(), expression);
        }
        return getSuccessors(instruction);
    }

    @Override
    public @NotNull List<DataFlowState> visitConnectInstruction(@NotNull ConnectInstruction instruction) {
        return getSuccessors(instruction);
    }

    @Override
    public @NotNull List<DataFlowState> visitConditionalBranchingInstruction(@NotNull ConditionalBranchingInstruction instruction) {
        Expression value = instruction.getCondition();
        List<DataFlowState> states = new ArrayList<>(2);
        if (instruction.getTrue() != null && instruction.getTrue().equals(instruction.getFalse())) {
            states.add(visitBranch(instruction.getTrue(), null));
        } else {
            Constraint constraint = state.getConstraint(value);
            if (constraint == Constraint.ANY_VALUE || constraint == Constraint.ALWAYS_TRUE) {
                states.add(visitBranch(instruction.getTrue(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(true))));
            }
            if (constraint == Constraint.ANY_VALUE || constraint == Constraint.ALWAYS_FALSE) {
                states.add(visitBranch(instruction.getFalse(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(false))));
            }
        }
        states.removeIf(Objects::isNull);
        return states;
    }

    private @Nullable DataFlowState visitBranch(@Nullable Instruction successor, @Nullable Expression condition) {
        if (successor == null) {
            return null;
        }
        if (condition != null) {
            DataFlowState successorState = DataFlowState.createSuccessorState(state.getInstruction(), state);
            successorState.add(condition);
            return DataFlowState.createSuccessorState(successor, successorState);
        } else {
            return DataFlowState.createSuccessorState(successor, state);
        }
    }

    private @NotNull List<DataFlowState> getSuccessors(@NotNull Instruction instruction) {
        List<DataFlowState> successors = new ArrayList<>();
        for (Instruction successor : instruction.getSuccessors()) {
            DataFlowState successorState = DataFlowState.createSuccessorState(successor, state);
            successors.add(successorState);
        }
        return successors;
    }

    @Override
    public @NotNull List<DataFlowState> visitReturnInstruction(@NotNull ReturnInstruction instruction) {
        Expression returnValue = instruction.getReturnValue();
        SnapshotExpression snapshot = getReferenceExpression(returnValue);
        if(isUnknownFunction()) {
            for (Argument argument : block.getControlFlow().getArguments()) {
                if(argument.getParameterType() != ParameterType.INPUT) {
                    SnapshotExpression expression = new SnapshotExpression(Snapshot.createSnapshot(argument.getType(), Optionality.UNKNOWN));
                    state.assign(new VariableExpression(argument), expression);

                }
            }
        }
        DataFlowState compactState = state.createCompactState(getTargets(snapshot));
        Snapshot returnSnapshot = snapshot != null ? snapshot.getSnapshot() : null;
        block.getFunction().registerOutput(state, new DataFlowFunction.Result.Success(compactState, returnSnapshot));
        return List.of();
    }

    private boolean isUnknownFunction() {
        RapidSymbol element = block.getControlFlow().getElement();
        if(!(element instanceof RapidRoutine routine)) {
            return false;
        }
        if(routine instanceof PhysicalRoutine) {
            return false;
        }
        for (HardcodedContract value : HardcodedContract.values()) {
            if(value.getRoutine().equals(routine)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull List<DataFlowState> visitExitInstruction(@NotNull ExitInstruction instruction) {
        DataFlowState successor = state.createCompactState(getTargets(null));
        block.getFunction().registerOutput(state, new DataFlowFunction.Result.Exit(successor));
        return List.of();
    }

    private @NotNull Set<Snapshot> getTargets(@Nullable SnapshotExpression variable) {
        Set<Snapshot> snapshots = new HashSet<>();
        if (variable != null) {
            snapshots.add(variable.getSnapshot());
        }
        Block block = state.getFunctionBlock();
        List<DataFlowState> chain = state.getPredecessorChain();
        for (ArgumentGroup argumentGroup : block.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                if(argument.getParameterType() != ParameterType.INPUT) {
                    Snapshot snapshot = chain.get(chain.size() - 1).getRoots().get(argument);
                    snapshots.add(snapshot);
                }
                SnapshotExpression snapshot = state.getSnapshot(new VariableExpression(argument));
                snapshots.add(snapshot.getSnapshot());
            }
        }
        return snapshots;
    }

    @Override
    public @NotNull List<DataFlowState> visitThrowInstruction(@NotNull ThrowInstruction instruction) {
        Expression exceptionValue = instruction.getExceptionValue();
        SnapshotExpression snapshot = getReferenceExpression(exceptionValue);
        DataFlowState compactState = state.createCompactState(getTargets(snapshot));
        block.getFunction().registerOutput(state, new DataFlowFunction.Result.Error(compactState, snapshot != null ? snapshot.getSnapshot() : null));
        return List.of();
    }

    private @Nullable SnapshotExpression getReferenceExpression(@Nullable Expression value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ReferenceExpression reference) {
            return state.getSnapshot(reference);
        }
        return state.createSnapshot(value);
    }

    private @NotNull List<DataFlowState> createSuccessorState(@NotNull Instruction successor) {
        DataFlowState successorState = DataFlowState.createSuccessorState(successor, state);
        return List.of(successorState);
    }

    @Override
    public @NotNull List<DataFlowState> visitErrorInstruction(@NotNull ErrorInstruction instruction) {
        Instruction successor = instruction.getSuccessor();
        if (successor != null) {
            return createSuccessorState(successor);
        }
        return List.of();
    }

    @Override
    public @NotNull List<DataFlowState> visitCallInstruction(@NotNull CallInstruction instruction) {
        ControlFlowService service = ControlFlowService.getInstance();
        Set<DataFlowFunction.Result> results = service.getDataFlowFunction(stack, state, instruction);
        List<DataFlowState> successors = new ArrayList<>();
        for (DataFlowFunction.Result result : results) {
            if (result instanceof DataFlowFunction.Result.Exit) {
                block.getFunction().registerOutput(state, result);
                continue;
            }
            if (result instanceof DataFlowFunction.Result.Error) {
                block.getFunction().registerOutput(state, result);
                continue;
            }
            successors.add(result.state());
        }
        Instruction successor = instruction.getSuccessor();
        return successors.stream().map(state -> DataFlowState.createSuccessorState(successor, state)).toList();
    }
}
