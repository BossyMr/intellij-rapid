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
    private final @NotNull Set<DataFlowAnalyzer.Entry> entries;

    public DataFlowAnalyzerVisitor(@NotNull Set<DataFlowAnalyzer.Entry> entries, @NotNull Set<RapidRoutine> stack, @NotNull DataFlowState state, @NotNull ControlFlowBlock block) {
        this.state = state;
        this.stack = stack;
        this.block = block;
        this.entries = entries;
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
        if (Objects.equals(instruction.getTrue(), instruction.getFalse())) {
            DataFlowState branch = visitBranch(instruction.getTrue(), null);
            return branch != null ? List.of(branch) : List.of();
        }
        Instruction commonSuccessor = getCommonSuccessor(instruction);
        boolean thenPure = commonSuccessor != null && instruction.getTrue() != null && isPureBranch(instruction.getTrue(), commonSuccessor);
        boolean elsePure = commonSuccessor != null && instruction.getFalse() != null && isPureBranch(instruction.getFalse(), commonSuccessor);
        List<DataFlowState> states = new ArrayList<>(2);
        Constraint constraint = state.getConstraint(value);
        if (constraint == Constraint.ANY_VALUE || constraint == Constraint.ALWAYS_TRUE) {
            DataFlowState branch = visitBranch(instruction.getTrue(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(true)));
            states.add(branch);
        }
        if (constraint == Constraint.ANY_VALUE || constraint == Constraint.ALWAYS_FALSE) {
            DataFlowState branch = visitBranch(instruction.getFalse(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(false)));
            states.add(branch);
        }
        states.removeIf(Objects::isNull);
        if (states.size() == 2 && thenPure && elsePure) {
            entries.add(new DataFlowAnalyzer.Entry(states.get(0), commonSuccessor));
        }
        return states;
    }

    private boolean isPureBranch(@NotNull Instruction instruction, @NotNull Instruction commonSuccessor) {
        if (instruction.equals(commonSuccessor)) {
            return true;
        }
        Deque<Instruction> deque = new ArrayDeque<>();
        deque.addLast(instruction);
        while (!(deque.isEmpty())) {
            Instruction nextInstruction = deque.removeFirst();
            if (nextInstruction instanceof TryNextInstruction || nextInstruction instanceof RetryInstruction) {
                return false;
            }
            if (nextInstruction instanceof ThrowInstruction || nextInstruction instanceof ExitInstruction) {
                return false;
            }
            if (nextInstruction instanceof AssignmentInstruction) {
                return false;
            }
            if (nextInstruction instanceof CallInstruction callInstruction) {
                ControlFlowService service = ControlFlowService.getInstance();
                if (service.hasSideEffect(block.getControlFlow(), callInstruction)) {
                    return false;
                }
            }
            for (Instruction successor : nextInstruction.getSuccessors()) {
                if (successor.equals(commonSuccessor)) {
                    continue;
                }
                deque.addLast(successor);
            }
        }
        return true;
    }

    private @Nullable Instruction getCommonSuccessor(@NotNull Instruction instruction) {
        return getCommonSuccessor(new HashSet<>(), instruction);
    }

    private @Nullable Instruction getCommonSuccessor(@NotNull Set<Instruction> visited, @NotNull Instruction instruction) {
        if (!(visited.add(instruction))) {
            return null;
        }
        List<Instruction> successors = instruction.getSuccessors();
        if (successors.isEmpty()) {
            return null;
        }
        if (instruction instanceof ConditionalBranchingInstruction) {
            if (successors.size() == 1) {
                return successors.get(0);
            }
            List<Instruction> successorChain = getCommonSuccessors(visited, successors.get(0));
            Instruction successor = successors.get(1);
            while (successor != null) {
                if (successorChain.contains(successor)) {
                    return successor;
                }
                successor = getCommonSuccessor(visited, successor);
            }
            return null;
        }
        if (successors.size() > 1) {
            return null;
        }
        return successors.get(0);
    }

    private @NotNull List<Instruction> getCommonSuccessors(@NotNull Set<Instruction> visited, @NotNull Instruction instruction) {
        List<Instruction> instructions = new ArrayList<>();
        Instruction successor = instruction;
        while ((successor = getCommonSuccessor(visited, successor)) != null) {
            instructions.add(successor);
        }
        return instructions;
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
        if (isUnknownFunction()) {
            for (Argument argument : block.getControlFlow().getArguments()) {
                if (argument.getParameterType() != ParameterType.INPUT) {
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
        if (!(element instanceof RapidRoutine routine)) {
            return false;
        }
        if (routine instanceof PhysicalRoutine) {
            return false;
        }
        for (HardcodedContract value : HardcodedContract.values()) {
            if (value.getRoutine().equals(routine)) {
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
                if (argument.getParameterType() != ParameterType.INPUT) {
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
