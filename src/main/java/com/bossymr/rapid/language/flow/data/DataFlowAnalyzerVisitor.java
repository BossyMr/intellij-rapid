package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.StringConstraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor {

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
    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        block.assign(new Condition(instruction.variable(), ConditionType.EQUALITY, instruction.value()));
    }

    @Override
    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {}

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        ReferenceValue value = instruction.value();
        Constraint constraint = block.getConstraint(value);
        if (constraint.contains(BooleanConstraint.alwaysTrue())) {
            Condition condition = new Condition(value, ConditionType.EQUALITY, Expression.of(true));
            block.addSuccessor(blocks.get(instruction.onSuccess()), condition);
        }
        if (constraint.contains(BooleanConstraint.alwaysFalse())) {
            Condition condition = new Condition(value, ConditionType.EQUALITY, Expression.of(false));
            block.addSuccessor(blocks.get(instruction.onFailure()), condition);
        }
    }

    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        block.addSuccessor(blocks.get(instruction.next()));
    }

    @Override
    public void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        super.visitRetryInstruction(instruction);
    }

    @Override
    public void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        super.visitTryNextInstruction(instruction);
    }

    @Override
    public void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        Map<Argument, Constraint> arguments = getArguments();
        ReferenceValue referenceValue = getReferenceValue(instruction.value());
        List<DataFlowState> states = block.getStates().stream()
                .map(DataFlowState::copy)
                .toList();
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Success(states, referenceValue));
    }

    @Override
    public void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        Map<Argument, Constraint> arguments = getArguments();
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Exit());
    }

    @Override
    public void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        Map<Argument, Constraint> arguments = getArguments();
        ReferenceValue referenceValue = getReferenceValue(instruction.exception());
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Error(block.getStates(), referenceValue));
    }

    private @Nullable ReferenceValue getReferenceValue(@Nullable Value variable) {
        if (variable instanceof ConstantValue constantValue) {
            VariableSnapshot snapshot = new VariableSnapshot(variable.getType());
            block.assign(new Condition(snapshot, ConditionType.EQUALITY, new ValueExpression(constantValue)));
            return snapshot;
        }
        if (variable instanceof ReferenceValue value) {
            return value;
        }
        if (variable == null) {
            return null;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        block.addSuccessor(blocks.get(instruction.next()));
    }

    @Override
    public void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        if (!(block.getConstraint(instruction.routine()) instanceof StringConstraint constraint)) {
            visitAnyCallInstruction(instruction);
        } else {
            for (String sequence : constraint.sequences()) {
                BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.element(), sequence);
                List<DataFlowState> states;
                if (instruction.routine() instanceof ReferenceValue referenceValue && constraint.sequences().size() > 1) {
                    states = block.split(new Condition(referenceValue, ConditionType.EQUALITY, Expression.of(sequence)));
                } else {
                    states = block.getStates();
                }
                if (blockDescriptor == null) {
                    /*
                     * The function call could not be found, assume that all provided arguments were modified.
                     */
                    visitAnyCallInstruction(instruction);
                    continue;
                }
                Optional<DataFlowFunction> function = functionMap.get(blockDescriptor);
                if (function.isEmpty()) {
                    PsiElement context = instruction.element();
                    RapidResolveService service = RapidResolveService.getInstance(context.getProject());
                    List<RapidSymbol> symbols = service.findSymbols(context, blockDescriptor.moduleName(), blockDescriptor.name());
                    if (symbols.size() == 1 && symbols.get(0) instanceof RapidRoutine routine) {
                        visitAnyCallInstruction(instruction, routine);
                    } else {
                        visitAnyCallInstruction(instruction);
                    }
                    continue;
                }
                processResult(instruction, states, function.orElseThrow(), getArguments(function.orElseThrow().getBlock(), instruction.arguments()));
            }
        }
    }

    private void visitAnyCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull RapidRoutine routine) {
        ReferenceValue returnValue = instruction.returnValue();
        Map<RapidParameter, Value> parameters = getParameters(routine, instruction.arguments());
        DataFlowBlock successor = blocks.get(instruction.next());
        List<DataFlowState> states = block.getStates().stream()
                .map(state -> DataFlowState.createSuccessorState(functionBlock, state))
                .peek(state -> {
                    if (returnValue != null) {
                        state.assign(returnValue, Constraint.any(returnValue.getType()));
                    }
                    for (var entry : parameters.entrySet()) {
                        if (entry.getKey().getParameterType() != ParameterType.INPUT) {
                            Value argument = entry.getValue();
                            if (argument instanceof ReferenceValue referenceValue) {
                                state.assign(referenceValue, Constraint.any(argument.getType()));
                            }
                        }
                    }
                }).toList();
        block.addSuccessor(successor, states);
    }

    private void visitAnyCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        ReferenceValue returnValue = instruction.returnValue();
        DataFlowBlock successor = blocks.get(instruction.next());
        List<DataFlowState> states = block.getStates().stream()
                .map(state -> DataFlowState.createSuccessorState(functionBlock, state))
                .peek(state -> {
                    if (returnValue != null) {
                        state.assign(returnValue, Constraint.any(returnValue.getType()));
                    }
                    for (Value value : instruction.arguments().values()) {
                        if (value instanceof ReferenceValue referenceValue) {
                            state.assign(referenceValue, Constraint.any(value.getType()));
                        }
                    }
                }).toList();
        block.addSuccessor(successor, states);
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

    private void processResult(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull List<DataFlowState> states, @NotNull DataFlowFunction function, @NotNull Map<Argument, Value> arguments) {
        Map<Argument, Constraint> constraints = new HashMap<>(arguments.size(), 1);
        arguments.forEach((argument, value) -> constraints.put(argument, block.getConstraint(value)));
        Set<DataFlowFunction.Result> results = function.getOutput(block, constraints);
        BlockDescriptor blockKey = BlockDescriptor.getBlockKey(functionBlock);
        for (DataFlowFunction.Result result : results) {
            if (result instanceof DataFlowFunction.Result.Exit) {
                functionMap.set(blockKey, block, getArguments(), new DataFlowFunction.Result.Exit());
                continue;
            }
            List<DataFlowState> resultStates;
            ReferenceValue resultValue;
            if (result instanceof DataFlowFunction.Result.Error error) {
                resultStates = error.states();
                resultValue = error.exceptionValue();
            } else if (result instanceof DataFlowFunction.Result.Success success) {
                resultStates = success.states();
                resultValue = success.returnValue();
            } else {
                throw new IllegalArgumentException();
            }
            DataFlowBlock successor = result instanceof DataFlowFunction.Result.Error ? block : blocks.get(instruction.next());
            List<DataFlowState> merged = states.stream().mapMulti((DataFlowState state, Consumer<DataFlowState> consumer) -> {
                for (DataFlowState resultState : resultStates) {
                    DataFlowState copy = DataFlowState.createSuccessorState(functionBlock, state);
                    copy.merge(resultState, arguments, resultValue, instruction.returnValue());
                    consumer.accept(copy);
                }
            }).toList();
            if (result instanceof DataFlowFunction.Result.Error) {
                functionMap.set(blockKey, block, getArguments(), new DataFlowFunction.Result.Error(merged, resultValue));
            } else {
                block.addSuccessor(successor, merged);
            }
        }
    }

    private @NotNull Map<Argument, Value> getArguments(@NotNull Block.FunctionBlock functionBlock, @NotNull Map<ArgumentDescriptor, Value> values) {
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        Map<Argument, Value> result = new HashMap<>();
        values.forEach((index, value) -> {
            Argument argument;
            if (index instanceof ArgumentDescriptor.Required required) {
                argument = arguments.get(required.index());
            } else if (index instanceof ArgumentDescriptor.Optional optional) {
                argument = arguments.stream()
                        .filter(element -> element.name().equals(optional.name()))
                        .findFirst()
                        .orElseThrow();
            } else {
                throw new AssertionError();
            }
            result.put(argument, value);
        });
        return result;
    }

    private @NotNull Map<RapidParameter, Value> getParameters(@NotNull RapidRoutine routine, @NotNull Map<ArgumentDescriptor, Value> values) {
        Map<RapidParameter, Value> result = new HashMap<>();
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

    private @NotNull Map<Argument, Constraint> getArguments() {
        Map<Argument, Constraint> constraints = new HashMap<>();
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        for (Argument argument : arguments) {
            constraints.put(argument, block.getConstraint(new VariableValue(argument)));
        }
        return constraints;
    }
}
