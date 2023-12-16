package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractDataFlowFunction implements DataFlowFunction {

    protected abstract @NotNull Set<DataFlowFunction.Result> getResults();

    @Override
    public @NotNull Set<Result> getOutput(@NotNull DataFlowState state, @NotNull CallInstruction instruction) {
        Set<DataFlowFunction.Result> results = new HashSet<>();
        for (Result result : getResults()) {
            Result output = getOutput(result, state, instruction);
            if (output != null) {
                results.add(output);
            }
        }
        return results;
    }

    protected @Nullable Result getOutput(@NotNull Result result, @NotNull DataFlowState state, @NotNull CallInstruction instruction) {
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
        Map<ReferenceExpression, ReferenceExpression> modifications = new HashMap<>();
        Set<ReferenceExpression> targets = new HashSet<>();

        Map<Argument, ReferenceExpression> arguments = getArguments(getBlock(), instruction.getArguments());
        for (Argument argument : arguments.keySet()) {
            ReferenceExpression expression = arguments.get(argument);
            SnapshotExpression calleeSnapshot = result.state().getSnapshot(new VariableExpression(argument));
            SnapshotExpression callerSnapshot = state.getSnapshot(expression);
            modifications.put(new VariableExpression(argument), expression);
            if(calleeSnapshot != null && callerSnapshot != null) {
                modifications.put(calleeSnapshot, callerSnapshot);
                targets.add(callerSnapshot);
            }
        }

        ReferenceExpression calleeVariable = result.variable();
        ReferenceExpression callerVariable = instruction.getReturnValue();
        SnapshotExpression callerSnapshot = null;
        if (calleeVariable != null && callerVariable != null) {
            SnapshotExpression calleeSnapshot = result.state().getSnapshot(calleeVariable);
            callerSnapshot = SnapshotExpression.createSnapshot(callerVariable);
            modifications.put(calleeVariable, callerVariable);
            if (calleeSnapshot != null) {
                modifications.put(calleeSnapshot, callerSnapshot);
            }
        }

        DataFlowState successorState = state.merge(result.state(), modifications);

        if (callerVariable != null && callerSnapshot != null) {
            successorState.assign(callerVariable, callerSnapshot);
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
            return new Result.Success(successorState, modifications.getOrDefault(calleeVariable, calleeVariable));
        }
        if (result instanceof Result.Error) {
            return new Result.Error(successorState, modifications.getOrDefault(calleeVariable, calleeVariable));
        }
        return null;
    }

    private <T> @NotNull Map<Argument, T> getArguments(@NotNull Block.FunctionBlock functionBlock, @NotNull Map<ArgumentDescriptor, T> values) {
        List<Argument> arguments = functionBlock.getArgumentGroups().stream()
                .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                .toList();
        Map<Argument, T> result = new HashMap<>();
        values.forEach((index, value) -> {
            Argument argument;
            if (index instanceof ArgumentDescriptor.Required required) {
                if (required.index() >= arguments.size()) {
                    return;
                } else {
                    argument = arguments.get(required.index());
                }
            } else if (index instanceof ArgumentDescriptor.Optional optional) {
                argument = arguments.stream()
                        .filter(element -> element.getName().equals(optional.name()))
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
