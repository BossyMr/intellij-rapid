package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor<Void> {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull DataFlowBlock block;
    private final @NotNull Map<BasicBlock, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;

    public DataFlowAnalyzerVisitor(@NotNull Block.FunctionBlock functionBlock, @NotNull DataFlowBlock block, @NotNull Map<BasicBlock, DataFlowBlock> blocks, @NotNull DataFlowFunctionMap functionMap) {
        this.functionBlock = functionBlock;
        this.block = block;
        this.blocks = blocks;
        this.functionMap = functionMap;
    }

    @Override
    public Void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        block.assign(instruction.variable(), instruction.value());
        return null;
    }

    @Override
    public Void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        return null;
    }

    @Override
    public Void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        Expression value = instruction.value();
        visitBranch(instruction.onSuccess(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new ConstantExpression(RapidPrimitiveType.BOOLEAN, true)));
        visitBranch(instruction.onFailure(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new ConstantExpression(RapidPrimitiveType.BOOLEAN, false)));
        return null;
    }

    private void visitBranch(@NotNull BasicBlock successor, @NotNull Expression condition) {
        DataFlowBlock successorBlock = blocks.get(successor);
        List<DataFlowState> states = block.getStates();
        List<DataFlowState> successors = new ArrayList<>(states.size());
        for (DataFlowState state : states) {
            DataFlowState copy = DataFlowState.createSuccessorState(successorBlock, state);
            copy.add(condition);
            if (!(copy.isSatisfiable())) {
                continue;
            }
            if (block.getHeads().isEmpty()) {
                successors.add(copy);
                continue;
            }
            for (BlockCycle blockCycle : block.getHeads()) {
                Optional<DataFlowState> previousCycle = getPreviousCycle(state, blockCycle);
                if (previousCycle.isEmpty()) {
                    // First iteration
                    successors.add(DataFlowState.copy(copy));
                    continue;
                }
                Optional<DataFlowState> thirdCycle = getPreviousCycle(previousCycle.orElseThrow(), blockCycle);
                if (thirdCycle.isEmpty()) {
                    // Second iteration
                    successors.add(DataFlowState.copy(copy));
                    continue;
                }
                // Third iteration
                if (isBlockCycle(successorBlock, blockCycle)) {
                    continue;
                }
                successors.add(DataFlowState.copy(copy));
            }
        }
        for (DataFlowState state : successors) {
            block.addSuccessor(successorBlock, state);
        }
    }

    private @NotNull Optional<DataFlowState> getPreviousCycle(@NotNull DataFlowState state, @NotNull BlockCycle blockCycle) {
        Optional<DataFlowBlock> originBlock = state.getBlock();
        if (originBlock.isEmpty()) {
            return Optional.empty();
        }
        do {
            Optional<DataFlowState> predecessor = state.getPredecessor();
            if (predecessor.isEmpty()) {
                return Optional.empty();
            }
            state = predecessor.orElseThrow();
            Optional<DataFlowBlock> currentBlock = state.getBlock();
            if (currentBlock.isEmpty()) {
                return Optional.empty();
            }
            if (currentBlock.equals(originBlock)) {
                return Optional.of(state);
            }
        } while (isBlockCycle(state, blockCycle));
        return Optional.empty();
    }

    private boolean isBlockCycle(@NotNull DataFlowState state, @NotNull BlockCycle blockCycle) {
        return state.getBlock()
                .map(block -> isBlockCycle(block, blockCycle))
                .orElse(false);

    }

    private boolean isBlockCycle(@NotNull DataFlowBlock block, @NotNull BlockCycle blockCycle) {
        return blockCycle.getSequence().contains(block);
    }

    @Override
    public Void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        block.addSuccessor(blocks.get(instruction.next()));
        return null;
    }

    @Override
    public Void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        super.visitRetryInstruction(instruction);
        return null;
    }

    @Override
    public Void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        super.visitTryNextInstruction(instruction);
        return null;
    }

    @Override
    public Void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactSuccessor();
            if (instruction.value() != null) {
                SnapshotExpression snapshot = successor.createSnapshot(instruction.value().getType(), null);
                successor.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, instruction.value()));
                functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Success(successor, snapshot));
            } else {
                functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Success(successor, null));
            }
        }
        return null;
    }

    @Override
    public Void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactSuccessor();
            functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Exit(successor));
        }
        return null;
    }

    @Override
    public Void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactSuccessor();
            if (instruction.exception() != null) {
                SnapshotExpression snapshot = successor.createSnapshot(instruction.exception().getType(), null);
                successor.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, instruction.exception()));
                functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Success(successor, snapshot));
            } else {
                functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Success(successor, null));
            }
        }
        return null;
    }

    @Override
    public Void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        DataFlowBlock successor = blocks.get(instruction.next());
        if (successor != null) {
            block.addSuccessor(successor);
        }
        return null;
    }

    @Override
    public Void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        Expression expression = instruction.routine();
        DataFlowBlock successor = blocks.get(instruction.next());
        for (DataFlowState state : block.getStates()) {
            if (!(expression instanceof ConstantExpression solution)) {
                visitAnyCallInstruction(instruction, state);
                continue;
            }
            if (!(solution.getValue() instanceof String routineName)) {
                continue;
            }
            BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.element(), routineName);
            if (blockDescriptor == null) {
                functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, new DataFlowFunction.Result.Error(state, null));
                continue;
            }
            Optional<DataFlowFunction> optional = functionMap.get(blockDescriptor);
            RapidRoutine routine = getRoutine(instruction, blockDescriptor);
            if (optional.isEmpty()) {
                if (routine != null) {
                    visitAnyCallInstruction(instruction, state, routine);
                } else {
                    visitAnyCallInstruction(instruction, state);
                }
                continue;
            }
            DataFlowFunction function = optional.orElseThrow();
            Set<DataFlowFunction.Result> results = function.getOutput(state, instruction);
            for (DataFlowFunction.Result result : results) {
                if (result instanceof DataFlowFunction.Result.Exit) {
                    functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, result);
                    continue;
                }
                if (result instanceof DataFlowFunction.Result.Error) {
                    functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, result);
                    continue;
                }
                block.addSuccessor(successor, result.state());
            }
        }
        return null;
    }

    private @Nullable RapidRoutine getRoutine(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull BlockDescriptor blockDescriptor) {
        PsiElement context = instruction.element();
        RapidResolveService service = RapidResolveService.getInstance(context.getProject());
        List<RapidSymbol> symbols = service.findSymbols(context, blockDescriptor.moduleName(), blockDescriptor.name());
        if (symbols.size() == 1 && symbols.get(0) instanceof RapidRoutine routine) {
            return routine;
        } else {
            return null;
        }
    }

    private void visitAnyCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull DataFlowState state, @NotNull RapidRoutine routine) {
        ReferenceExpression returnValue = instruction.returnValue();
        Map<RapidParameter, ReferenceExpression> parameters = getParameters(routine, instruction.arguments());
        DataFlowBlock successor = blocks.get(instruction.next());
        DataFlowState successorState = DataFlowState.createSuccessorState(successor, state);
        if (returnValue != null) {
            state.createSnapshot(returnValue);
        }
        for (var entry : parameters.entrySet()) {
            if (entry.getKey().getParameterType() != ParameterType.INPUT) {
                Expression argument = entry.getValue();
                if (argument instanceof ReferenceExpression referenceExpression) {
                    state.createSnapshot(referenceExpression);
                }
            }
        }
        block.addSuccessor(successor, successorState);
    }

    private void visitAnyCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull DataFlowState state) {
        ReferenceExpression returnValue = instruction.returnValue();
        DataFlowBlock successor = blocks.get(instruction.next());
        if (returnValue != null) {
            state.createSnapshot(returnValue);
        }
        for (Expression value : instruction.arguments().values()) {
            if (value instanceof ReferenceExpression referenceExpression) {
                state.createSnapshot(referenceExpression);
            }
        }
        block.addSuccessor(successor, state);
    }

    private @Nullable BlockDescriptor getBlockDescriptor(@NotNull PsiElement context, @NotNull String text) {
        String[] strings = text.split(":");
        if (strings.length == 2) {
            return new BlockDescriptor(strings[0], strings[1]);
        }
        if (strings.length != 1) {
            return null;
        }
        List<RapidSymbol> symbols = RapidResolveService.getInstance(context.getProject()).findSymbols(context, text);
        if (symbols.isEmpty()) {
            return null;
        }
        if (!(symbols.get(0) instanceof RapidRoutine routine)) {
            return null;
        }
        String name = routine.getName();
        if (name == null) {
            return null;
        }
        if (routine instanceof PhysicalElement element) {
            PhysicalModule module = PhysicalModule.getModule(element);
            if (module == null) {
                return null;
            }
            String moduleName = module.getName();
            if (moduleName == null) {
                return null;
            }
            return new BlockDescriptor(moduleName, name);
        } else {
            return new BlockDescriptor("", name);
        }
    }

    private <T> @NotNull Map<RapidParameter, T> getParameters(@NotNull RapidRoutine routine, @NotNull Map<ArgumentDescriptor, T> values) {
        Map<RapidParameter, T> result = new HashMap<>();
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters == null) {
            return result;
        }
        List<? extends RapidParameter> arguments = parameters.stream()
                .flatMap(parameterGroup -> parameterGroup.getParameters().stream())
                .toList();
        values.forEach((index, value) -> {
            RapidParameter argument;
            if (index instanceof ArgumentDescriptor.Required required) {
                argument = arguments.get(required.index());
            } else if (index instanceof ArgumentDescriptor.Optional optional) {
                argument = arguments.stream()
                        .filter(element -> Objects.equals(element.getName(), optional.name()))
                        .findFirst()
                        .orElseThrow();
            } else {
                throw new AssertionError();
            }
            result.put(argument, value);
        });
        return result;
    }
}
