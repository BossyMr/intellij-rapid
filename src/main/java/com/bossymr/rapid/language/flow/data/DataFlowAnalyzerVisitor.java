package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.BooleanValue;
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

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor<List<DataFlowState>> {

    private final @NotNull DataFlowAnalyzer analyzer;

    private final @NotNull DataFlowState state;
    private final @NotNull Map<Instruction, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;

    public DataFlowAnalyzerVisitor(@NotNull DataFlowAnalyzer analyzer, @NotNull DataFlowState state, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull DataFlowFunctionMap functionMap) {
        this.analyzer = analyzer;
        this.state = state;
        this.blocks = blocks;
        this.functionMap = functionMap;
    }

    @Override
    public @NotNull List<DataFlowState> visitAssignmentInstruction(@NotNull AssignmentInstruction instruction) {
        DataFlowState previousCycle = DataFlowAnalyzer.getPreviousCycle(state);
        if (previousCycle != null) {
            /*
             * If this block is in a loop (i.e. a state with this instruction is a predecessor to this state) and
             * this assignment is cyclic in nature: this assignment has the previous snapshot (or a snapshot
             * which is newer than the root snapshot of the previous iteration) of the variable which is being
             * assigned in the expression. For example, x2 := x1 + 1 or x3 := x1 + 1 (if x1 is the latest snapshot
             * for x at that point).
             */
            state.assign(instruction.getVariable(), null);
        } else {
            state.assign(instruction.getVariable(), instruction.getExpression());
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
        BooleanValue constraint = state.getConstraint(value);
        List<DataFlowState> states = new ArrayList<>(2);
        if (constraint == BooleanValue.ANY_VALUE || constraint == BooleanValue.ALWAYS_TRUE) {
            states.add(visitBranch(instruction.getTrue(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(true))));
        }
        if (constraint == BooleanValue.ANY_VALUE || constraint == BooleanValue.ALWAYS_FALSE) {
            states.add(visitBranch(instruction.getFalse(), new BinaryExpression(BinaryOperator.EQUAL_TO, value, new LiteralExpression(false))));
        }
        states.removeIf(Objects::isNull);
        return states;
    }

    private @Nullable DataFlowState visitBranch(@Nullable Instruction successor, @NotNull Expression condition) {
        if (successor == null) {
            return null;
        }
        DataFlowBlock successorBlock = blocks.get(successor);
        DataFlowState successorState = DataFlowState.createSuccessorState(successorBlock, state);
        successorState.add(condition);
        return successorState;
    }

    private @NotNull List<DataFlowState> getSuccessors(@NotNull Instruction instruction) {
        List<DataFlowState> successors = new ArrayList<>();
        for (Instruction successor : instruction.getSuccessors()) {
            DataFlowBlock successorBlock = blocks.get(successor);
            DataFlowState successorState = DataFlowState.createSuccessorState(successorBlock, state);
            successors.add(successorState);
        }
        return successors;
    }

    @Override
    public @NotNull List<DataFlowState> visitReturnInstruction(@NotNull ReturnInstruction instruction) {
        Expression returnValue = instruction.getReturnValue();
        ReferenceExpression snapshot = getReferenceExpression(returnValue);
        DataFlowState compactState = state.createCompactState();
        functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, new DataFlowFunction.Result.Success(compactState, snapshot));
        return List.of();
    }

    @Override
    public @NotNull List<DataFlowState> visitExitInstruction(@NotNull ExitInstruction instruction) {
        DataFlowState successor = state.createCompactState();
        functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, new DataFlowFunction.Result.Exit(successor));
        return List.of();
    }

    @Override
    public @NotNull List<DataFlowState> visitThrowInstruction(@NotNull ThrowInstruction instruction) {
        Expression exceptionValue = instruction.getExceptionValue();
        ReferenceExpression snapshot = getReferenceExpression(exceptionValue);
        DataFlowState compactState = state.createCompactState();
        functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, new DataFlowFunction.Result.Error(compactState, snapshot));
        return List.of();
    }

    private @Nullable ReferenceExpression getReferenceExpression(Expression exceptionValue) {
        if (exceptionValue == null) {
            return null;
        }
        ReferenceExpression snapshot = state.createSnapshot(exceptionValue.getType(), null);
        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, exceptionValue));
        return snapshot;
    }

    private @NotNull List<DataFlowState> createSuccessorState(@NotNull Instruction successor) {
        DataFlowBlock successorBlock = blocks.get(successor);
        DataFlowState successorState = DataFlowState.createSuccessorState(successorBlock, state);
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
        Expression expression = instruction.getRoutineName();
        if (!(expression instanceof LiteralExpression solution)) {
            visitAnyCallInstruction(instruction, state);
            return createSuccessorState(instruction.getSuccessor());
        }
        if (!(solution.getValue() instanceof String routineName)) {
            return createSuccessorState(instruction.getSuccessor());
        }
        BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.getElement(), routineName);
        if (blockDescriptor == null) {
            functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, new DataFlowFunction.Result.Error(state, null));
            return createSuccessorState(instruction.getSuccessor());
        }
        Optional<DataFlowFunction> optional = functionMap.get(blockDescriptor);
        RapidRoutine routine = getRoutine(instruction, blockDescriptor);
        if (optional.isEmpty()) {
            if (routine != null) {
                visitAnyCallInstruction(instruction, state, routine);
            } else {
                visitAnyCallInstruction(instruction, state);
            }
            return createSuccessorState(instruction.getSuccessor());
        }
        DataFlowFunction function = optional.orElseThrow();
        Set<DataFlowFunction.Result> results = function.getOutput(state, instruction);
        List<DataFlowState> successors = new ArrayList<>();
        for (DataFlowFunction.Result result : results) {
            if (result instanceof DataFlowFunction.Result.Exit) {
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, result);
                continue;
            }
            if (result instanceof DataFlowFunction.Result.Error) {
                functionMap.set(BlockDescriptor.getBlockKey(analyzer.getFunctionBlock()), state, result);
                continue;
            }
            DataFlowState successorState = DataFlowState.createSuccessorState(blocks.get(instruction.getSuccessor()), result.state());
            successors.add(successorState);
        }
        return successors;
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
