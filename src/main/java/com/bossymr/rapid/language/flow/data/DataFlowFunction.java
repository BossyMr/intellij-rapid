package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataFlowFunction {

    private final @NotNull ControlFlowBlock block;
    private final @NotNull Map<DataFlowState, Set<DataFlowState>> usages = new WeakHashMap<>();
    private final @NotNull Map<DataFlowState, Result> results = new HashMap<>();

    private final @NotNull Map<Field, Snapshot> snapshots = new HashMap<>();
    private @Nullable Snapshot returnVariable;

    public DataFlowFunction(@NotNull ControlFlowBlock block) {
        if (!(block.getControlFlow() instanceof Block.FunctionBlock)) {
            throw new IllegalArgumentException("Could not create function for block: " + block);
        }
        this.block = block;
    }

    public static @NotNull Result getDefaultOutput(@Nullable Block.FunctionBlock functionBlock, @NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        ReferenceExpression returnValue = instruction.getReturnValue();
        DataFlowState successorState = DataFlowState.createSuccessorState(callerState.getInstruction(), callerState);
        if (functionBlock != null && returnValue == null) {
            Set<Argument> arguments = getArguments(functionBlock, instruction.getArguments()).keySet();
            if (arguments.stream().allMatch(argument -> argument.getParameterType() == ParameterType.INPUT)) {
                /*
                 * The method is pure, i.e. it does not have a return value, and it does not modify any passed
                 * arguments. Therefore, it doesn't matter what happens in the method: it won't have any effect on
                 * the caller.
                 */
                return new Result.Success(successorState, null);
            }
        }
        Snapshot snapshot = returnValue != null ? Snapshot.createSnapshot(returnValue.getType()) : null;
        if (returnValue != null) {
            successorState.assign(returnValue, new SnapshotExpression(snapshot));
        }
        Map<ArgumentDescriptor, Expression> arguments = instruction.getArguments();
        for (Expression argument : arguments.values()) {
            if (argument instanceof ReferenceExpression variable) {
                successorState.assign(variable, null);
            }
        }
        return new Result.Success(successorState, snapshot);
    }

    public static <T> @NotNull Map<Argument, T> getArguments(@NotNull Block functionBlock, @NotNull Map<ArgumentDescriptor, T> values) {
        Map<String, Argument> optionalArguments = new HashMap<>();
        List<Argument> requiredArguments = new ArrayList<>();
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            if (argumentGroup.isOptional()) {
                for (Argument argument : argumentGroup.arguments()) {
                    optionalArguments.put(argument.getName(), argument);
                }
            } else if (!argumentGroup.arguments().isEmpty()) {
                requiredArguments.add(argumentGroup.arguments().get(0));
            }
        }
        Map<Argument, T> result = new HashMap<>();
        values.forEach((descriptor, value) -> {
            if (descriptor instanceof ArgumentDescriptor.Required required) {
                if (required.index() < requiredArguments.size()) {
                    result.put(requiredArguments.get(required.index()), value);
                }
            } else if (descriptor instanceof ArgumentDescriptor.Optional optional) {
                if (optionalArguments.containsKey(optional.name())) {
                    result.put(optionalArguments.get(optional.name()), value);
                }
            } else {
                throw new AssertionError();
            }
        });
        return result;
    }

    public @NotNull Map<DataFlowState, Set<DataFlowState>> getUsages() {
        return usages;
    }

    public @NotNull Map<DataFlowState, Result> getResults() {
        return results;
    }

    public @NotNull Block.FunctionBlock getBlock() {
        return (Block.FunctionBlock) block.getControlFlow();
    }

    public void registerOutput(@NotNull DataFlowState returnState, @NotNull Result result) {
        Result normalized = normalizeResult(result);
        if (results.containsValue(normalized)) {
            return;
        }
        results.put(returnState, normalized);
    }

    private @NotNull Result normalizeResult(@NotNull Result result) {
        if (!(result instanceof Result.Success)) {
            return result;
        }
        DataFlowState state = new DataFlowState(result.state().getBlock(), result.state().getInstruction(), null);
        for (Expression expression : result.state().getConditions()) {
            state.getConditions().add(expression.replace(component -> {
                if (!(component instanceof SnapshotExpression snapshot)) {
                    return component;
                }
                return new SnapshotExpression(normalizeSnapshot(result, snapshot.getSnapshot(), state));
            }));
        }
        Map<Field, Snapshot> snapshots = result.state().getSnapshots();
        for (Field field : snapshots.keySet()) {
            state.getSnapshots().put(field, normalizeSnapshot(result, snapshots.get(field), state));
        }
        Map<Field, Snapshot> roots = result.state().getRoots();
        for (Field field : roots.keySet()) {
            state.getRoots().put(field, normalizeSnapshot(result, roots.get(field), state));
        }
        return new Result.Success(state, result.variable() != null ? normalizeSnapshot(result, result.variable(), state) : null);
    }

    private @NotNull Snapshot normalizeSnapshot(@NotNull Result result, @NotNull Snapshot snapshot, @NotNull DataFlowState state) {
        Field field = getField(result.state(), snapshot);
        if (Objects.equals(snapshot, result.variable())) {
            if (returnVariable == null) {
                returnVariable = Snapshot.createSnapshot(snapshot.getType(), Optionality.PRESENT);
            }
        }
        if (field != null) {
            Snapshot normalized = snapshots.computeIfAbsent(field, key -> Snapshot.createSnapshot(snapshot.getType(), snapshot.getOptionality()));
            if (Objects.equals(snapshot, result.variable())) {
                Objects.requireNonNull(returnVariable);
                state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(normalized), new SnapshotExpression(returnVariable)));
            }
            return normalized;
        }
        if (Objects.equals(snapshot, result.variable())) {
            Objects.requireNonNull(returnVariable);
            return returnVariable;
        }
        return snapshot;
    }

    private @Nullable Field getField(@NotNull DataFlowState state, @NotNull Snapshot snapshot) {
        for (Field field : state.getSnapshots().keySet()) {
            Snapshot normalized = state.getSnapshots().get(field);
            if (normalized.equals(snapshot)) {
                return field;
            }
        }
        return null;
    }

    public void unregisterOutput(@NotNull DataFlowState returnState) {
        results.remove(returnState);
        usages.values().removeIf(value -> {
            value.remove(returnState);
            return value.isEmpty();
        });
    }

    public @NotNull Set<Result> getOutput(@NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        Set<Result> output = new HashSet<>();
        Map<Argument, Expression> arguments = getArguments(getBlock(), instruction.getArguments());
        Block controlFlow = block.getControlFlow();
        for (ArgumentGroup argumentGroup : controlFlow.getArgumentGroups()) {
            if (argumentGroup.isOptional() || argumentGroup.arguments().isEmpty()) {
                continue;
            }
            if (argumentGroup.arguments().stream().noneMatch(arguments::containsKey)) {
                return Set.of(new Result.Error(callerState.createSuccessorState()));
            }
        }
        boolean requiresArgumentHandling = arguments.values().stream().anyMatch(expression -> !(expression instanceof ReferenceExpression));
        DataFlowState state = requiresArgumentHandling ? callerState.createSuccessorState() : callerState;
        if (requiresArgumentHandling) {
            for (Argument argument : arguments.keySet()) {
                Expression expression = arguments.get(argument);
                if (expression instanceof ReferenceExpression) {
                    continue;
                }
                Snapshot snapshot = Snapshot.createSnapshot(expression.getType());
                SnapshotExpression snapshotExpression = new SnapshotExpression(snapshot);
                state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshotExpression, expression));
                arguments.put(argument, snapshotExpression);
            }
        }
        Map<Result, DataFlowState> mappings = new HashMap<>();
        for (Iterator<Map.Entry<DataFlowState, Result>> iterator = results.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<DataFlowState, Result> entry = iterator.next();
            Result result = entry.getValue();
            boolean isSatisfiable = output.isEmpty() && !(iterator.hasNext());
            Result actual = getOutput(result, state.createSuccessorState(), instruction, arguments, isSatisfiable);
            if (actual != null) {
                output.add(actual);
                mappings.put(actual, entry.getKey());
                Set<DataFlowState> collection = Objects.requireNonNullElseGet(usages.get(state), HashSet::new);
                collection.add(entry.getKey());
            }
        }
        if (!(ControlFlowService.getInstance().hasSideEffect(block.getControlFlow(), instruction))) {
            output.removeIf(result -> {
                if (result instanceof Result.Success) {
                    Set<DataFlowState> states = usages.get(state);
                    states.remove(mappings.get(result));
                    if (states.isEmpty()) {
                        usages.remove(state);
                    }
                    Objects.requireNonNullElseGet(result.state().getPredecessor(), result::state).prune();
                    return true;
                }
                return false;
            });
            output.add(new Result.Success(callerState.createSuccessorState(), null));
        }
        return output;
    }

    protected @Nullable Result getOutput(@NotNull Result result, @NotNull DataFlowState state, @NotNull CallInstruction instruction, @NotNull Map<Argument, Expression> arguments, boolean isSatisfiable) {
        /*
         * Create an empty successor to the specified state.
         *
         * Add all conditions from the result state to that state, but modify all variables and snapshots:
         *      1. Parameters should be replaced by their respective arguments.
         *
         * Modify the snapshot for the callerVariable calleeVariable:
         *      - Assign the return calleeVariable of the result state to the callerVariable calleeVariable of the calling state.
         *
         * Modify snapshots for arguments to parameters which are not of type INPUT:
         *      - Assign argument to the modified snapshot of the snapshot for the parameter.
         *
         */

        /*
         * A map which contains instructions to replace the key expression with the value expression.
         */
        Map<Snapshot, Snapshot> modifications = new HashMap<>();
        Set<Snapshot> targets = new HashSet<>();

        for (Argument argument : arguments.keySet()) {
            ReferenceExpression variable;
            Expression expression = arguments.get(argument);
            if (expression instanceof ReferenceExpression referenceExpression) {
                variable = referenceExpression;
            } else {
                variable = new SnapshotExpression(Snapshot.createSnapshot(expression.getType()), expression);
                state.assign(variable, expression);
            }
            Snapshot calleeSnapshot = result.state().getRoots().get(argument);
            if (calleeSnapshot == null) {
                continue;
            }
            Snapshot callerSnapshot = state.getSnapshot(variable).getSnapshot();
            modifications.put(calleeSnapshot, callerSnapshot);
            targets.add(callerSnapshot);
        }

        DataFlowState successorState = state.merge(result.state(), modifications);

        Snapshot calleeSnapshot = result.variable();
        ReferenceExpression callerVariable = instruction.getReturnValue();
        if (calleeSnapshot != null && callerVariable != null) {
            successorState.assign(callerVariable, new SnapshotExpression(calleeSnapshot));

        }

        for (Argument argument : arguments.keySet()) {
            if (argument.getParameterType() == ParameterType.INPUT) {
                continue;
            }
            Snapshot latestSnapshot = result.state().getSnapshots().get(argument);
            if (latestSnapshot == null) {
                continue;
            }
            Snapshot snapshot = modifications.getOrDefault(latestSnapshot, latestSnapshot);
            Expression expression = arguments.get(argument);
            if (expression instanceof ReferenceExpression variable) {
                successorState.assign(variable, new SnapshotExpression(snapshot));
                Snapshot root = successorState.getRoot(variable);
                if (root != null) {
                    successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.present(snapshot), FunctionCallExpression.present(root)));
                }
            }
        }

        // Check if the result is satisfiable.
        // If it is not satisfiable, this function will not be called.
        if (!(isSatisfiable) && !(successorState.isSatisfiable(targets))) {
            state.prune();
            return null;
        }
        if (result instanceof Result.Exit) {
            return new Result.Exit(successorState);
        }
        if (result instanceof Result.Success) {
            return new Result.Success(successorState, modifications.getOrDefault(calleeSnapshot, calleeSnapshot));
        }
        if (result instanceof Result.Error) {
            return new Result.Error(successorState, modifications.getOrDefault(calleeSnapshot, calleeSnapshot));
        }
        return null;
    }

    public sealed interface Result {


        @NotNull DataFlowState state();

        @Nullable Snapshot variable();

        record Success(@NotNull DataFlowState state, @Nullable Snapshot variable) implements Result {}

        record Error(@NotNull DataFlowState state, @Nullable Snapshot variable) implements Result {

            public Error(@NotNull DataFlowState state) {
                this(state, Snapshot.createSnapshot(RapidPrimitiveType.NUMBER));
            }
        }

        record Exit(@NotNull DataFlowState state) implements Result {
            @Override
            public @Nullable Snapshot variable() {
                return null;
            }
        }
    }

}
