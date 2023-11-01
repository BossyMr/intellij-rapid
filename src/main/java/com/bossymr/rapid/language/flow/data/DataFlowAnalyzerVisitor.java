package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor<Void> {

    private final @NotNull DataFlowAnalyzer analyzer;

    private final @NotNull DataFlowBlock block;
    private final @NotNull Map<Instruction, DataFlowBlock> blocks;
    private final @NotNull Set<BlockCycle> cycles;
    private final @NotNull DataFlowFunctionMap functionMap;

    public DataFlowAnalyzerVisitor(@NotNull DataFlowAnalyzer analyzer, @NotNull DataFlowBlock block, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Set<BlockCycle> cycles, @NotNull DataFlowFunctionMap functionMap) {
        this.analyzer = analyzer;
        this.block = block;
        this.blocks = blocks;
        this.cycles = cycles;
        this.functionMap = functionMap;
    }

    @Override
    public Void visitAssignmentInstruction(@NotNull AssignmentInstruction instruction) {
        block.assign(instruction.getVariable(), instruction.getExpression());
        addSuccessors(instruction);
        return null;
    }

    @Override
    public Void visitConnectInstruction(@NotNull ConnectInstruction instruction) {
        addSuccessors(instruction);
        return null;
    }

    @Override
    public Void visitConditionalBranchingInstruction(@NotNull ConditionalBranchingInstruction instruction) {
        Expression value = instruction.getCondition();
        visitBranch(instruction, instruction.getTrue(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new ConstantExpression(true)));
        visitBranch(instruction, instruction.getFalse(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new ConstantExpression(false)));
        return null;
    }

    private void visitBranch(@NotNull ConditionalBranchingInstruction instruction, @Nullable Instruction successor, @NotNull Expression condition) {
        if (successor == null) {
            return;
        }
        DataFlowBlock successorBlock = blocks.get(successor);
        List<DataFlowState> states = block.getStates();
        for (DataFlowState state : states) {
            DataFlowState copy = DataFlowState.createSuccessorState(successorBlock, state);
            copy.add(condition);
            if (!(copy.isSatisfiable())) {
                continue;
            }
            addSuccessor(instruction, successor, copy);
        }
    }

    private void addSuccessors(@NotNull Instruction instruction) {
        for (Instruction successor : instruction.getSuccessors()) {
            DataFlowBlock successorBlock = blocks.get(successor);
            for (DataFlowState state : block.getStates()) {
                DataFlowState successorState = DataFlowState.createSuccessorState(successorBlock, state);
                addSuccessor(instruction, successor, successorState);
            }
        }
    }

    private void addSuccessor(@NotNull Instruction instruction, @NotNull Instruction successor, @NotNull DataFlowState successorState) {
        Set<BlockCycle> successorCycles = getCycles(successor);
        successorCycles.removeAll(getCycles(instruction));
        DataFlowBlock successorBlock = blocks.get(successor);
        if(successorCycles.isEmpty()) {
            block.addSuccessor(successorBlock, successorState);
            return;
        }
        // Process regularly (but isolated) until a state is reached which isn't in any cycle, then, save it in the
        // exit map. Once the workList is empty (all cycles have reached an exit), return - with modification for
        // infinite loops without any exits at all.
        Map<Instruction, DataFlowBlock> original = new HashMap<>();

        for (BlockCycle blockCycle : successorCycles) {

            Map<Instruction, Set<DataFlowState>> exits = analyzer.processCycle(successorBlock, successorState, blockCycle);
            for (Instruction exitInstruction : exits.keySet()) {
                Set<DataFlowState> exitStates = exits.get(exitInstruction);
                for (DataFlowState exitState : exitStates) {
                    block.addSuccessor(successorBlock, exitState);
                }
            }
        }
    }

    private @NotNull Set<BlockCycle> getCycles(@NotNull Instruction instruction) {
        return cycles.stream()
                .filter(cycle -> cycle.instructions().contains(instruction))
                .collect(Collectors.toSet());
    }

    @Override
    public Void visitReturnInstruction(@NotNull ReturnInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactState();
            Expression returnValue = instruction.getReturnValue();
            if (returnValue != null) {
                SnapshotExpression snapshot = successor.createSnapshot(returnValue.getType(), null);
                successor.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, returnValue));
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Success(successor, snapshot));
            } else {
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Success(successor, null));
            }
        }
        return null;
    }

    @Override
    public Void visitExitInstruction(@NotNull ExitInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactState();
            functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Exit(successor));
        }
        return null;
    }

    @Override
    public Void visitThrowInstruction(@NotNull ThrowInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            DataFlowState successor = state.createCompactState();
            Expression exceptionValue = instruction.getExceptionValue();
            if (exceptionValue != null) {
                SnapshotExpression snapshot = successor.createSnapshot(exceptionValue.getType(), null);
                successor.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, exceptionValue));
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Error(successor, snapshot));
            } else {
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Error(successor, null));
            }
        }
        return null;
    }

    @Override
    public Void visitErrorInstruction(@NotNull ErrorInstruction instruction) {
        DataFlowBlock successor = blocks.get(instruction.getSuccessor());
        if (successor != null) {
            block.addSuccessor(successor);
        }
        return null;
    }

    @Override
    public Void visitCallInstruction(@NotNull CallInstruction instruction) {
        Expression expression = instruction.getRoutineName();
        for (DataFlowState state : block.getStates()) {
            if (!(expression instanceof ConstantExpression solution)) {
                visitAnyCallInstruction(instruction, state);
                continue;
            }
            if (!(solution.getValue() instanceof String routineName)) {
                continue;
            }
            BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.getElement(), routineName);
            if (blockDescriptor == null) {
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, new DataFlowFunction.Result.Error(state, null));
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
                    functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, result);
                    continue;
                }
                if (result instanceof DataFlowFunction.Result.Error) {
                    functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), this.block, result);
                    continue;
                }
                addSuccessor(instruction, instruction.getSuccessor(), result.state());
            }
        }
        return null;
    }

    private @Nullable RapidRoutine getRoutine(@NotNull CallInstruction instruction, @NotNull BlockDescriptor blockDescriptor) {
        PsiElement context = instruction.getElement();
        if (context == null) {
            return null;
        }
        RapidResolveService service = RapidResolveService.getInstance(context.getProject());
        List<RapidSymbol> symbols = service.findSymbols(context, blockDescriptor.moduleName(), blockDescriptor.name());
        if (symbols.size() == 1 && symbols.get(0) instanceof RapidRoutine routine) {
            return routine;
        } else {
            return null;
        }
    }

    private void visitAnyCallInstruction(@NotNull CallInstruction instruction, @NotNull DataFlowState state, @NotNull RapidRoutine routine) {
        ReferenceExpression returnValue = instruction.getReturnValue();
        Map<RapidParameter, ReferenceExpression> parameters = getParameters(routine, instruction.getArguments());
        DataFlowBlock successor = blocks.get(instruction.getSuccessor());
        DataFlowState successorState = DataFlowState.createSuccessorState(successor, state);
        if (returnValue != null) {
            state.assign(returnValue, null);
        }
        for (var entry : parameters.entrySet()) {
            if (entry.getKey().getParameterType() != ParameterType.INPUT) {
                Expression argument = entry.getValue();
                if (argument instanceof ReferenceExpression referenceExpression) {
                    state.assign(referenceExpression, null);
                }
            }
        }
        addSuccessor(instruction, instruction.getSuccessor(), successorState);
    }

    private void visitAnyCallInstruction(@NotNull CallInstruction instruction, @NotNull DataFlowState state) {
        ReferenceExpression returnValue = instruction.getReturnValue();
        if (returnValue != null) {
            state.assign(returnValue, null);
        }
        for (Expression value : instruction.getArguments().values()) {
            if (value instanceof ReferenceExpression referenceExpression) {
                state.assign(referenceExpression, null);
            }
        }
        addSuccessor(instruction, instruction.getSuccessor(), state);
    }

    private @Nullable BlockDescriptor getBlockDescriptor(@Nullable PsiElement context, @NotNull String text) {
        String[] strings = text.split(":");
        if (strings.length == 2) {
            return new BlockDescriptor(strings[0], strings[1]);
        }
        if (strings.length != 1) {
            return null;
        }
        if (context == null) {
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
