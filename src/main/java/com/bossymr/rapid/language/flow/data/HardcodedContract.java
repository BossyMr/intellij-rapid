package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Argument;
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
            (block, callerState, instruction, arguments) -> {
                Map<String, Expression> parameters = new HashMap<>();
                arguments.forEach((argument, expression) -> parameters.put(argument.getName(), expression));
                Expression expression = parameters.get("OptPar");
                if (!(expression instanceof ReferenceExpression referenceExpression)) {
                    DataFlowState successorState = callerState.createSuccessorState();
                    Snapshot snapshot = Snapshot.createSnapshot(RapidPrimitiveType.BOOLEAN);
                    successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), new LiteralExpression(true)));
                    return Set.of(new DataFlowFunction.Result.Success(successorState, snapshot));
                }
                SnapshotExpression snapshotExpression = callerState.getSnapshot(referenceExpression);
                DataFlowState successorState = callerState.createSuccessorState();
                Snapshot snapshot = Snapshot.createSnapshot(RapidPrimitiveType.BOOLEAN);
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), FunctionCallExpression.present(snapshotExpression.getSnapshot())));
                return Set.of(new DataFlowFunction.Result.Success(successorState, snapshot));
            }),
    DIM(builder -> builder
            .withRoutine("Dim", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("ArrPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE)
                            .withParameter("DimNo", ParameterType.INPUT, RapidPrimitiveType.NUMBER))),
            (block, callerState, instruction, arguments) -> {
                RapidType returnType = Objects.requireNonNull(block.getControlFlow().getReturnType());
                Map<String, Expression> parameters = new HashMap<>();
                arguments.forEach((argument, expression) -> parameters.put(argument.getName(), expression));
                RapidExpression expression = instruction.getElement() instanceof RapidExpression element ? element : null;
                Expression parameter = parameters.get("ArrPar");
                if (!(parameter instanceof SnapshotExpression array) || !(array.getType().isArray())) {
                    return Set.of(new DataFlowFunction.Result.Error(callerState.createSuccessorState(), Snapshot.createSnapshot(RapidPrimitiveType.NUMBER)));
                }
                Set<DataFlowFunction.Result> results = new HashSet<>();
                Expression degree = parameters.get("DimNo");
                ReferenceExpression returnValue = instruction.getReturnValue();
                if (returnValue != null) {
                    SnapshotExpression snapshot = callerState.createSnapshot(returnValue);
                    Expression previousLength = null;
                    ReferenceExpression variable = array;
                    for (int i = 1; i <= 3; i++) {
                        variable = new IndexExpression(variable, new LiteralExpression(0));
                        DataFlowState successSuccessorState = callerState.createSuccessorState();
                        if (previousLength != null) {
                            successSuccessorState.add(new BinaryExpression(BinaryOperator.GREATER_THAN, previousLength, new LiteralExpression(0)));
                            DataFlowState errorSuccessorState = callerState.createSuccessorState();
                            errorSuccessorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, previousLength, new LiteralExpression(0)));
                            errorSuccessorState.add(new BinaryExpression(BinaryOperator.GREATER_THAN_OR_EQUAL, degree, new LiteralExpression(i)));
                            results.add(new DataFlowFunction.Result.Error(errorSuccessorState, Snapshot.createSnapshot(RapidPrimitiveType.NUMBER)));
                        }
                        successSuccessorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, degree, new LiteralExpression(i)));
                        previousLength = new FunctionCallExpression(expression, returnType, ":Dim", List.of(Entry.pointerOf(array.getSnapshot()), Entry.valueOf(degree)));
                        successSuccessorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, previousLength));
                        results.add(new DataFlowFunction.Result.Success(successSuccessorState, snapshot.getSnapshot()));
                    }
                }
                return results;
            }),
    COSINE(builder -> builder
            .withRoutine("Cos", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("Angle", ParameterType.INPUT, RapidPrimitiveType.NUMBER))),
            DataFlowProcessor.pureProcessor());

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
                Set<Result> output = processor.getOutput(block, callerState, instruction, getArguments(controlFlow, instruction.getArguments()));
                return output.stream()
                             .map(result -> {
                                 Result normalized = getOutput(result, callerState, instruction);
                                 result.state().prune();
                                 return normalized;
                             })
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

        static @NotNull DataFlowProcessor pureProcessor() {
            return (block, callerState, instruction, arguments) -> {
                Block controlFlow = block.getControlFlow();
                RapidType returnType = controlFlow.getReturnType();
                DataFlowState successorState = callerState.createSuccessorState();
                if (returnType == null) {
                    return Set.of(new DataFlowFunction.Result.Success(successorState, null));
                }
                String name = controlFlow.getModuleName() + ":" + controlFlow.getName();
                Snapshot snapshot = Snapshot.createSnapshot(returnType);
                List<Entry> entries = new ArrayList<>();
                List<Argument> parameters = controlFlow.getArguments();
                List<Map.Entry<Argument, Expression>> ordered = new ArrayList<>(arguments.entrySet());
                ordered.sort(Map.Entry.comparingByKey(Comparator.comparing(parameters::indexOf)));
                for (Map.Entry<Argument, Expression> argument : ordered) {
                    if (argument.getKey().getParameterType() == ParameterType.INPUT || !(argument.getValue() instanceof SnapshotExpression expression)) {
                        entries.add(new Entry.ValueEntry(argument.getValue()));
                        continue;
                    }
                    entries.add(new Entry.ReferenceEntry(expression.getSnapshot()));
                }
                FunctionCallExpression expression = new FunctionCallExpression(returnType, name, entries);
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), expression));
                return Set.of(new DataFlowFunction.Result.Success(successorState, snapshot));
            };
        }

        @NotNull Set<DataFlowFunction.Result> getOutput(@NotNull ControlFlowBlock block,
                                                        @NotNull DataFlowState callerState,
                                                        @NotNull CallInstruction instruction,
                                                        @NotNull Map<Argument, Expression> arguments);

    }
}
