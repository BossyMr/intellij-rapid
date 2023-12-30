package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataFlowFunction {

    private final @NotNull ControlFlowBlock block;
    private final @NotNull Map<DataFlowState, Set<DataFlowState>> usages = new WeakHashMap<>();
    private final @NotNull Map<DataFlowState, Result> results = new HashMap<>();

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
        Map<ArgumentDescriptor, ReferenceExpression> arguments = instruction.getArguments();
        for (ReferenceExpression argument : arguments.values()) {
            successorState.assign(argument, null);
        }
        return new Result.Success(successorState, snapshot);
    }

    public static <T> @NotNull Map<Argument, T> getArguments(@NotNull Block functionBlock, @NotNull Map<ArgumentDescriptor, T> values) {
        List<Argument> arguments = new ArrayList<>();
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            arguments.addAll(argumentGroup.arguments());
        }
        Map<Argument, T> result = new HashMap<>();
        values.forEach((descriptor, value) -> {
            if (descriptor instanceof ArgumentDescriptor.Required required) {
                if (required.index() < arguments.size()) {
                    result.put(arguments.get(required.index()), value);
                }
            } else if (descriptor instanceof ArgumentDescriptor.Optional optional) {
                for (Argument element : arguments) {
                    if (element.getName().equals(optional.name())) {
                        result.put(element, value);
                    }
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
        results.put(returnState, result);
    }

    public void unregisterOutput(@NotNull DataFlowState returnState) {
        results.remove(returnState);
        usages.values().removeIf(value -> value.contains(returnState));
    }

    public @NotNull Set<Result> getOutput(@NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        Map<Argument, ReferenceExpression> arguments = getArguments(getBlock(), instruction.getArguments());
        ReferenceExpression returnValue = instruction.getReturnValue();
        Set<Result> output = new HashSet<>();
        for (DataFlowState calleeState : results.keySet()) {
            Result result = results.get(calleeState);
            Result actual = getOutput(result, callerState, instruction);
            if (actual != null) {
                output.add(actual);
                usages.computeIfAbsent(callerState, key -> new HashSet<>());
                usages.get(callerState).add(calleeState);
            }
        }
        if (returnValue == null) {
            if (arguments.keySet().stream().allMatch(argument -> argument.getParameterType() == ParameterType.INPUT)) {
                /*
                 * This function call will not have any side effects on the caller. As a result, only at most a single
                 * empty success state needs to be returned.
                 */
                output.removeIf(result -> {
                    if (result instanceof Result.Success) {
                        result.state().close();
                        return true;
                    }
                    return false;
                });
                DataFlowState successorState = DataFlowState.createSuccessorState(callerState.getInstruction(), callerState);
                output.add(new Result.Success(successorState, null));
            }
        }
        return output;
    }

    private @Nullable Result getOutput(@NotNull Result result, @NotNull DataFlowState state, @NotNull CallInstruction instruction) {
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

        Map<Argument, ReferenceExpression> arguments = getArguments(getBlock(), instruction.getArguments());
        for (Argument argument : arguments.keySet()) {
            ReferenceExpression expression = arguments.get(argument);
            Snapshot calleeSnapshot = result.state().getRoots().get(argument);
            Snapshot callerSnapshot = state.getSnapshot(expression).getSnapshot();
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
            Snapshot snapshot = modifications.get(latestSnapshot);
            ReferenceExpression expression = arguments.get(argument);
            successorState.assign(expression, new SnapshotExpression(snapshot));
        }

        // Check if the result is satisfiable.
        // If it is not satisfiable, this function will not be called.
        if (!(successorState.isSatisfiable(targets))) {
            successorState.close();
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

        record Error(@NotNull DataFlowState state, @Nullable Snapshot variable) implements Result {}

        record Exit(@NotNull DataFlowState state) implements Result {
            @Override
            public @Nullable Snapshot variable() {
                return null;
            }
        }
    }

}
