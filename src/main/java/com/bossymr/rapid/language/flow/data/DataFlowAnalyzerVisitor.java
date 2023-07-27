package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.constraint.StringConstraint;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataFlowAnalyzerVisitor extends ControlFlowVisitor {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull DataFlowBlock block;
    private final @NotNull Map<BasicBlock, DataFlowBlock> blocks;
    private final @NotNull DataFlowFunctionMap functionMap;

    public DataFlowAnalyzerVisitor(@NotNull Block.FunctionBlock functionBlock,
                                   @NotNull DataFlowBlock block,
                                   @NotNull Map<BasicBlock, DataFlowBlock> blocks,
                                   @NotNull DataFlowFunctionMap functionMap) {
        this.functionBlock = functionBlock;
        this.block = block;
        this.blocks = blocks;
        this.functionMap = functionMap;
    }

    @Override
    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        for (DataFlowState state : block.getStates()) {
            state.assign(instruction.variable(), instruction.value());
        }
    }

    @Override
    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {}

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        ReferenceValue value = instruction.value();
        Constraint constraint = block.getConstraint(value);
        if (constraint.contains(BooleanConstraint.alwaysTrue())) {
            block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.onSuccess()), getStates(value, true)));
        }
        if (constraint.contains(BooleanConstraint.alwaysFalse())) {
            block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.onFailure()), getStates(value, false)));
        }
    }

    private @NotNull Set<DataFlowState> getStates(@NotNull ReferenceValue value, boolean result) {
        return block.getStates().stream()
                .map(DataFlowState::createCopy)
                .filter(state -> state.intersects(value, BooleanConstraint.withValue(result)))
                .peek(state -> state.add(new Condition(value, ConditionType.EQUALITY, Expression.booleanConstant(result))))
                .collect(Collectors.toSet());
    }


    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.next()), block.getStates()));
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
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Success(block.getStates(), referenceValue));
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
            for (DataFlowState state : block.getStates()) {
                state.assign(snapshot, new VariableExpression(constantValue));
            }
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
        block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.next()), block.getStates()));
    }

    @Override
    public void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        Value routine = instruction.routine();
        if (routine instanceof ConstantValue) {
            getEarlyBoundCall(instruction);
        } else {
            getLateBoundCall(instruction);
        }
    }

    private @Nullable BlockDescriptor getBlockDescriptor(@NotNull PsiElement context, @NotNull String text) {
        String[] strings = text.split(":");
        if (strings.length == 2) {
            return new BlockDescriptor(strings[0], strings[1]);
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

    private void getLateBoundCall(@NotNull BranchingInstruction.CallInstruction instruction) {
        if (!(instruction.routine() instanceof ReferenceValue referenceValue)) {
            throw new AssertionError();
        }
        if (!(block.getConstraint(referenceValue) instanceof StringConstraint constraint)) {
            Set<DataFlowState> states = block.getStates().stream()
                    .map(DataFlowState::createCopy)
                    .peek(state -> {
                        if (instruction.returnValue() != null) {
                            state.assign(instruction.returnValue(), Constraint.any(instruction.returnValue().getType()));
                        }
                        for (Value value : instruction.arguments().values()) {
                            if (value instanceof ReferenceValue argumentValue) {
                                state.assign(argumentValue, Constraint.any(argumentValue.getType()));
                            }
                        }
                    })
                    .collect(Collectors.toSet());
            block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.next()), states));
            return;
        }
        for (String sequence : constraint.sequences()) {
            BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.element(), sequence);
            if (blockDescriptor == null) {
                continue;
            }
            DataFlowFunction function = functionMap.get(block, blockDescriptor);
            Set<DataFlowState> states = block.getStates().stream()
                    .map(DataFlowState::createCopy)
                    .filter(state -> state.intersects(referenceValue, new StringConstraint(Optionality.PRESENT, Set.of(sequence))))
                    .peek(state -> state.assign(referenceValue, new VariableExpression(new ConstantValue(RapidType.STRING, sequence))))
                    .collect(Collectors.toSet());
            processResult(instruction, states, function, getArguments(function.getBlock(), instruction.arguments()));
        }
    }

    private void getEarlyBoundCall(@NotNull BranchingInstruction.CallInstruction instruction) {
        if (!(instruction.routine() instanceof ConstantValue value)) {
            throw new AssertionError();
        }
        if (!(value.value() instanceof String text)) {
            throw new AssertionError();
        }
        BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.element(), text);
        if (blockDescriptor == null) {
            throw new IllegalStateException("Method: " + text + " was not found");
        }
        DataFlowFunction function = functionMap.get(block, blockDescriptor);
        processResult(instruction, block.getStates(), function, getArguments(function.getBlock(), instruction.arguments()));
    }

    private void processResult(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull Set<DataFlowState> states, @NotNull DataFlowFunction function, @NotNull Map<Argument, ReferenceValue> arguments) {
        Map<Argument, Constraint> constraints = new HashMap<>(arguments.size(), 1);
        arguments.forEach((argument, value) -> constraints.put(argument, block.getConstraint(value)));
        Set<DataFlowFunction.Result> results = function.getOutput(constraints);
        BlockDescriptor blockKey = BlockDescriptor.getBlockKey(functionBlock);
        for (DataFlowFunction.Result result : results) {
            if (result instanceof DataFlowFunction.Result.Exit) {
                functionMap.set(blockKey, block, getArguments(), new DataFlowFunction.Result.Exit());
                continue;
            }
            Set<DataFlowState> resultStates;
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
            Set<DataFlowState> merged = states.stream().mapMulti((DataFlowState state, Consumer<DataFlowState> consumer) -> {
                for (DataFlowState resultState : resultStates) {
                    DataFlowState copy = DataFlowState.createCopy(state);
                    copy.merge(resultState, arguments, resultValue, instruction.returnValue());
                    consumer.accept(copy);
                }
            }).collect(Collectors.toSet());
            if (result instanceof DataFlowFunction.Result.Error) {
                functionMap.set(blockKey, block, getArguments(), new DataFlowFunction.Result.Error(merged, resultValue));
            } else {
                block.getSuccessors().add(new DataFlowEdge(blocks.get(instruction.next()), merged));
            }
        }
    }

    private @NotNull Map<Argument, ReferenceValue> getArguments(@NotNull Block.FunctionBlock functionBlock, @NotNull Map<ArgumentDescriptor, ReferenceValue> values) {
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        Map<Argument, ReferenceValue> result = new HashMap<>();
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
