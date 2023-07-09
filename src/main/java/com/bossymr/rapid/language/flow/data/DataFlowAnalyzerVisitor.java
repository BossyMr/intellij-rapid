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
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        VariableSnapshot snapshot = new VariableSnapshot(instruction.variable().type());
        Condition condition = new Condition(snapshot, ConditionType.EQUALITY, instruction.value());
        for (DataFlowState state : block.states()) {
            for (Condition result : condition.solve()) {
                state.setCondition(result);
            }
            state.snapshots().put(instruction.variable(), snapshot);
        }
    }

    @Override
    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {}

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        ReferenceValue value = instruction.value();
        Constraint constraint = block.getConstraint(value);
        if (!(constraint instanceof BooleanConstraint booleanConstraint)) {
            throw new IllegalStateException();
        }
        block.successors().add(new DataFlowEdge(booleanConstraint.contains(BooleanConstraint.alwaysTrue()), blocks.get(instruction.onSuccess()), getStates(value, true)));
        block.successors().add(new DataFlowEdge(booleanConstraint.contains(BooleanConstraint.alwaysFalse()), blocks.get(instruction.onFailure()), getStates(value, false)));
    }

    private @NotNull Set<DataFlowState> getStates(@NotNull ReferenceValue value, boolean result) {
        return block.states().stream()
                .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                .filter(state -> state.intersects(value, state.getConstraint(value)))
                .peek(state -> {
                    List<Condition> ascertain = ascertain(state, new VariableExpression(value), result);
                    for (Condition child : ascertain) {
                        state.setCondition(child);
                    }
                })
                .collect(Collectors.toSet());
    }

    public @NotNull List<Condition> ascertain(@NotNull DataFlowState state, @NotNull Expression expression, boolean result) {
        List<Condition> conditions = new ArrayList<>();
        expression.accept(new ControlFlowVisitor() {
            @Override
            public void visitVariableExpression(@NotNull VariableExpression expression) {
                Value value = expression.value();
                assert value.type().isAssignable(RapidType.BOOLEAN);
                if (!(value instanceof ReferenceValue referenceValue)) {
                    return;
                }
                conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, result))));
                List<Condition> children = state.getConditions(referenceValue);
                for (Condition child : children) {
                    conditions.addAll(ascertain(state, child.getExpression(), result));
                }
            }

            @Override
            public void visitBinaryExpression(@NotNull BinaryExpression expression) {
                Value left = expression.left();
                if (!(left instanceof ReferenceValue referenceValue)) {
                    return;
                }
                switch (expression.operator()) {
                    case LESS_THAN ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.LESS_THAN, new VariableExpression(expression.right())), result);
                    case LESS_THAN_OR_EQUAL ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.LESS_THAN_OR_EQUAL, new VariableExpression(expression.right())), result);
                    case EQUAL_TO ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(expression.right())), result);
                    case NOT_EQUAL_TO ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.INEQUALITY, new VariableExpression(expression.right())), result);
                    case GREATER_THAN ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.GREATER_THAN, new VariableExpression(expression.right())), result);
                    case GREATER_THAN_OR_EQUAL ->
                            getCondition(conditions, new Condition(referenceValue, ConditionType.GREATER_THAN_OR_EQUAL, new VariableExpression(expression.right())), result);
                    case AND -> {
                        if (result) {
                            conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            if (expression.right() instanceof ReferenceValue right) {
                                conditions.add(new Condition(right, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            }
                        }
                    }
                }
            }

            @Override
            public void visitUnaryExpression(@NotNull UnaryExpression expression) {
                Value value = expression.value();
                if (!(value instanceof ReferenceValue referenceValue)) {
                    return;
                }
                if (expression.operator() == UnaryOperator.NOT) {
                    conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, !result))));
                    List<Condition> children = state.getConditions(referenceValue);
                    for (Condition child : children) {
                        conditions.addAll(ascertain(state, child.getExpression(), !result));
                    }
                }
            }
        });
        return List.copyOf(conditions);
    }

    private void getCondition(@NotNull List<Condition> conditions, @NotNull Condition condition, boolean result) {
        Condition value = result ? condition : condition.negate();
        conditions.add(value);
    }


    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        block.successors().add(new DataFlowEdge(true, blocks.get(instruction.next()), block.states()));
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
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Success(block.states(), referenceValue));
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
        functionMap.set(BlockDescriptor.getBlockKey(functionBlock), block, arguments, new DataFlowFunction.Result.Error(block.states(), referenceValue));
    }

    private @Nullable ReferenceValue getReferenceValue(@Nullable Value exception) {
        if (exception instanceof ConstantValue constantValue) {
            ReferenceValue referenceValue = new VariableSnapshot(constantValue.type());
            block.add(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(constantValue)));
            return referenceValue;
        }
        if (exception instanceof ReferenceValue value) {
            return value;
        }
        if (exception == null) {
            return null;
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        block.successors().add(new DataFlowEdge(false, blocks.get(instruction.next()), block.states()));
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
            Set<DataFlowState> states = block.states().stream()
                    .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                    .peek(state -> {
                        if (instruction.returnValue() != null) {
                            state.getConditions(instruction.returnValue()).forEach(state.conditions()::remove);
                        }
                        for (Value value : instruction.arguments().values()) {
                            if (value instanceof ReferenceValue argumentValue) {
                                state.getConditions(argumentValue).forEach(state.conditions()::remove);
                            }
                        }
                    })
                    .collect(Collectors.toSet());
            block.successors().add(new DataFlowEdge(true, blocks.get(instruction.next()), states));
            return;
        }
        for (String sequence : constraint.sequences()) {
            BlockDescriptor blockDescriptor = getBlockDescriptor(instruction.element(), sequence);
            if(blockDescriptor == null) {
                continue;
            }
            DataFlowFunction function = functionMap.get(block, blockDescriptor);
            Set<DataFlowState> states = block.states().stream()
                    .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                    .filter(state -> state.intersects(referenceValue, new StringConstraint(Optionality.PRESENT, Set.of(sequence))))
                    .peek(state -> state.setCondition(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.STRING, sequence)))))
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
            throw new AssertionError();
        }
        DataFlowFunction function = functionMap.get(block, blockDescriptor);
        processResult(instruction, block.states(), function, getArguments(function.getBlock(), instruction.arguments()));
    }

    private void processResult(@NotNull BranchingInstruction.CallInstruction instruction, @NotNull Set<DataFlowState> states, @NotNull DataFlowFunction function, @NotNull Map<Argument, Value> arguments) {
        Map<Argument, Constraint> constraints = new HashMap<>(arguments.size(), 1);
        arguments.forEach((argument, value) -> constraints.put(argument, block.getConstraint(value)));
        Set<DataFlowFunction.Result> results = function.getOutput(constraints);
        for (DataFlowFunction.Result result : results) {
            if (!(result instanceof DataFlowFunction.Result.Success success)) {
                continue;
            }
            List<DataFlowState> trimmed = success.states().stream()
                    .peek(state -> {
                        Set<ReferenceValue> references = new HashSet<>();
                        Deque<ReferenceValue> queue = new ArrayDeque<>();
                        Map<ReferenceValue, ReferenceValue> snapshots = new HashMap<>();
                        if (success.returnValue() != null) {
                            queue.add(state.getValue(success.returnValue()));
                        }
                        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
                            for (Argument argument : argumentGroup.arguments()) {
                                VariableReference value = new VariableReference(argument);
                                if (argument.parameterType() != ParameterType.INPUT) {
                                    // Any modification to this argument will be kept, so the latest snapshot for the
                                    // argument needs to be updated to match the snapshot for the argument.
                                    if(arguments.containsKey(argument)) {
                                        snapshots.put((ReferenceValue) arguments.get(argument), state.getValue(value));
                                    }
                                }
                                queue.add(state.getValue(value));
                            }
                        }
                        while (!(queue.isEmpty())) {
                            ReferenceValue referenceValue = queue.removeFirst();
                            for (Condition condition : state.getConditions(referenceValue)) {
                                for (ReferenceValue value : condition.collect()) {
                                    if (!(references.contains(value))) {
                                        queue.add(referenceValue);
                                    }
                                }
                            }
                            references.add(referenceValue);
                        }
                        state.conditions().removeIf(condition -> !(references.contains(condition.getVariable())));
                        state.snapshots().clear();
                        state.snapshots().putAll(snapshots);
                    })
                    .toList();
            Set<DataFlowState> next = states.stream()
                    .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                    .flatMap(state -> {
                        Stream.Builder<DataFlowState> builder = Stream.builder();
                        for (DataFlowState temporary : trimmed) {
                            DataFlowState copy = new DataFlowState(state.conditions(), state.snapshots());
                            for (Condition condition : temporary.conditions()) {
                                copy.setCondition(condition);
                            }
                            copy.snapshots().putAll(temporary.snapshots());
                            builder.accept(temporary);
                        }
                        return builder.build();
                    })
                    .collect(Collectors.toSet());
            block.successors().add(new DataFlowEdge(true, blocks.get(instruction.next()), next));
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

    private @NotNull Map<Argument, Constraint> getArguments() {
        Map<Argument, Constraint> constraints = new HashMap<>();
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        for (Argument argument : arguments) {
            constraints.put(argument, block.getConstraint(new VariableReference(argument)));
        }
        return constraints;
    }
}
