package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.expression.FunctionCallExpression.Entry;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HardcodedContract {

    PRESENT(builder -> builder
            .withRoutine("Present", RoutineType.FUNCTION, RapidPrimitiveType.BOOLEAN, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("OptPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE))),
            context -> {
                Expression expression = context.getArgument("OptPar");
                DataFlowState successorState = context.getCallerState().createSuccessorState();
                Snapshot returnValue = Snapshot.createSnapshot(RapidPrimitiveType.BOOLEAN);
                if (!(expression instanceof SnapshotExpression variable)) {
                    successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(returnValue), new LiteralExpression(true)));
                    return Set.of(new DataFlowFunction.Result.Success(successorState, returnValue));
                }
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(returnValue), FunctionCallExpression.present(variable.getSnapshot())));
                return Set.of(new DataFlowFunction.Result.Success(successorState, returnValue));
            }),
    DIM(builder -> builder
            .withRoutine("Dim", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("ArrPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE))
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("DimNo", ParameterType.INPUT, RapidPrimitiveType.NUMBER))),
            context -> {
                Expression argument = context.getArgument("ArrPar");
                DataFlowState callerState = context.getCallerState();
                if (!(argument instanceof SnapshotExpression array) || !(array.getType().isArray())) {
                    return Set.of(new DataFlowFunction.Result.Error(callerState.createSuccessorState(), Snapshot.createSnapshot(RapidPrimitiveType.NUMBER)));
                }
                Snapshot returnValue = Snapshot.createSnapshot(RapidPrimitiveType.NUMBER);
                Expression degree = context.getArgument("DimNo");
                Set<DataFlowFunction.Result> results = new HashSet<>();
                ReferenceExpression variable = array;
                Expression previousLength = null;
                RapidExpression element = context.getInstruction().getElement() instanceof RapidExpression expression ? expression : null;
                for (int i = 1; i <= 3; i++) {
                    DataFlowState successState = callerState.createSuccessorState();
                    if (previousLength != null) {
                        successState.add(new BinaryExpression(BinaryOperator.GREATER_THAN, previousLength, new LiteralExpression(0)));
                        DataFlowState errorState = callerState.createSuccessorState();
                        errorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, degree, new LiteralExpression(i)));
                        errorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, previousLength, new LiteralExpression(0)));
                        results.add(new DataFlowFunction.Result.Error(errorState));
                    }
                    // To retrieve the 1st, 2nd, or 3rd the specified degree must be equal to the current degree
                    successState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, degree, new LiteralExpression(i)));
                    previousLength = new FunctionCallExpression(element, RapidPrimitiveType.NUMBER, ":Dim", List.of(Entry.pointerOf(array.getSnapshot()), Entry.valueOf(new LiteralExpression(i))));
                    successState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(returnValue), previousLength));
                    results.add(new DataFlowFunction.Result.Success(successState, returnValue));
                    array = callerState.getSnapshot(new IndexExpression(variable, new LiteralExpression(0)));
                }
                DataFlowState errorState = callerState.createSuccessorState();
                errorState.add(new BinaryExpression(BinaryOperator.OR, new BinaryExpression(BinaryOperator.LESS_THAN, degree, new LiteralExpression(0)), new BinaryExpression(BinaryOperator.GREATER_THAN, degree, new LiteralExpression(3))));
                results.add(new DataFlowFunction.Result.Error(errorState));
                return results;
            }),
    COSINE(builder -> builder
            .withRoutine("Cos", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("Angle", ParameterType.INPUT, RapidPrimitiveType.NUMBER))),
            DataFlowProcessor.pureMethod());

    private final @NotNull VirtualRoutine routine;
    private final @NotNull Block controlFlow;
    private final @NotNull Function<ControlFlowBlock, DataFlowFunction> function;

    HardcodedContract(@NotNull Consumer<RapidModuleBuilder> consumer) {
        this.controlFlow = computeBlock(consumer);
        this.routine = (VirtualRoutine) controlFlow.getElement();
        this.function = DataFlowFunction::new;
    }

    HardcodedContract(@NotNull Consumer<RapidModuleBuilder> consumer, @NotNull DataFlowProcessor processor) {
        this.controlFlow = computeBlock(consumer);
        this.function = block -> new DataFlowFunction(block) {
            @Override
            public @NotNull Set<Result> getOutput(@NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
                DataFlowContext context = new DataFlowContext(block, callerState, instruction);
                Map<Argument, Expression> arguments = context.getArguments();
                for (ArgumentGroup argumentGroup : context.getControlFlow().getArgumentGroups()) {
                    if (argumentGroup.isOptional() || argumentGroup.arguments().isEmpty()) {
                        continue;
                    }
                    if (argumentGroup.arguments().stream().noneMatch(arguments::containsKey)) {
                        return Set.of(new Result.Error(callerState.createSuccessorState()));
                    }
                }
                Set<Result> output = processor.getOutput(context);
                return output.stream()
                             .map(result -> {
                                 Result normalized = getOutput(result, callerState, instruction, arguments, false);
                                 result.state().prune();
                                 return normalized;
                             })
                             .filter(Objects::nonNull)
                             .collect(Collectors.toSet());
            }
        };
        this.routine = (VirtualRoutine) controlFlow.getElement();
    }

    private static @NotNull Block computeBlock(@NotNull Consumer<RapidModuleBuilder> consumer) {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        builder.withModule("", consumer);
        Set<Block> controlFlow = builder.getControlFlow();
        if (controlFlow.size() != 1) {
            throw new IllegalArgumentException();
        }
        return controlFlow.iterator().next();
    }

    public @NotNull VirtualRoutine getRoutine() {
        return routine;
    }

    public @NotNull Block getBlock() {
        return controlFlow;
    }

    public @NotNull DataFlowFunction getFunction(@NotNull ControlFlowBlock block) {
        return function.apply(block);
    }

    @FunctionalInterface
    private interface DataFlowProcessor {

        static @NotNull DataFlowProcessor pureMethod() {
            return context -> {
                Block controlFlow = context.getBlock().getControlFlow();
                RapidType returnType = controlFlow.getReturnType();
                DataFlowState successorState = context.getCallerState().createSuccessorState();
                if (returnType == null) {
                    return Set.of(new DataFlowFunction.Result.Success(successorState, null));
                }
                Snapshot returnValue = Snapshot.createSnapshot(returnType);
                FunctionCallExpression expression = context.createFunctionCallExpression();
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(returnValue), expression));
                return Set.of(new DataFlowFunction.Result.Success(successorState, returnValue));
            };
        }

        @NotNull Set<DataFlowFunction.Result> getOutput(@NotNull DataFlowContext context);
    }

    public static final class DataFlowContext {

        private final @NotNull ControlFlowBlock block;
        private final @NotNull DataFlowState callerState;
        private final @NotNull CallInstruction instruction;

        private final @NotNull Map<Argument, Expression> arguments;
        private final @NotNull Map<String, Expression> argumentsByName;

        public DataFlowContext(@NotNull ControlFlowBlock block, @NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
            this.block = block;
            this.callerState = callerState;
            this.instruction = instruction;
            this.arguments = DataFlowFunction.getArguments(block.getControlFlow(), instruction.getArguments());
            arguments.replaceAll((argument, expression) -> callerState.getSnapshot(expression));
            this.argumentsByName = new HashMap<>();
            arguments.forEach((argument, expression) -> argumentsByName.put(argument.getName(), expression));
        }

        public @NotNull FunctionCallExpression createFunctionCallExpression() {
            RapidType returnType = getControlFlow().getReturnType();
            if (returnType == null) {
                throw new IllegalStateException("Cannot create function call expression for block: " + getControlFlow());
            }
            RapidExpression element = instruction.getElement() instanceof RapidExpression expression ? expression : null;
            String functionName = getControlFlow().getModuleName() + ":" + getControlFlow().getName();
            return new FunctionCallExpression(element, returnType, functionName, getEntries());
        }

        public @NotNull List<Entry> getEntries() {
            List<Entry> entries = new ArrayList<>();
            ArrayList<Map.Entry<Argument, Expression>> ordered = new ArrayList<>(arguments.entrySet());
            ordered.sort(Map.Entry.comparingByKey(Comparator.comparing(argument -> getControlFlow().getArguments().indexOf(argument))));
            for (Map.Entry<Argument, Expression> entry : ordered) {
                if (entry.getKey().getParameterType() == ParameterType.INPUT) {
                    entries.add(new Entry.ValueEntry(entry.getValue()));
                } else if (!(entry.getValue() instanceof SnapshotExpression snapshot)) {
                    entries.add(new Entry.ValueEntry(entry.getValue()));
                } else {
                    entries.add(new Entry.ReferenceEntry(snapshot.getSnapshot()));
                }
            }
            return entries;
        }

        public @NotNull Map<Argument, Expression> getArguments() {
            return arguments;
        }

        public Expression getArgument(@NotNull String name) {
            return argumentsByName.get(name);
        }

        public @NotNull ControlFlowBlock getBlock() {
            return block;
        }

        public @NotNull Block.FunctionBlock getControlFlow() {
            return (Block.FunctionBlock) getBlock().getControlFlow();
        }

        public @NotNull DataFlowState getCallerState() {
            return callerState;
        }

        public @NotNull CallInstruction getInstruction() {
            return instruction;
        }
    }
}
