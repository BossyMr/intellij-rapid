package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
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
         * Modify the snapshot for the target variable:
         *      - Assign the return variable of the result state to the target variable of the calling state.
         *
         * Modify snapshots for arguments to parameters which are not of type INPUT:
         *      - Assign argument to the modified snapshot of the snapshot for the parameter.
         *
         */

        /*
         * A map which contains instructions to replace the key expression with the value expression.
         */
        Map<ReferenceExpression, ReferenceExpression> modifications = new HashMap<>();

        Map<Argument, ReferenceExpression> arguments = getArguments(getBlock(), instruction.getArguments());
        for (Argument argument : arguments.keySet()) {
            ReferenceExpression expression = arguments.get(argument);
            modifications.put(new VariableExpression(argument), expression);
        }

        DataFlowState successorState = state.merge(result.state(), modifications);

        // Assign target to the output of the result.
        ReferenceExpression variable = result.variable();
        ReferenceExpression target = instruction.getReturnValue();
        if (variable != null && target != null) {
            Optional<SnapshotExpression> optional = result.state().getSnapshot(variable);
            ReferenceExpression value;
            if (optional.isPresent()) {
                SnapshotExpression expression = optional.orElseThrow();
                value = modifications.getOrDefault(expression, expression);
            } else {
                value = variable;
            }
            successorState.assign(target, value);
        }

        for (Argument argument : arguments.keySet()) {
            ReferenceExpression expression = arguments.get(argument);
            if (argument.getParameterType() != ParameterType.INPUT) {
                VariableExpression variableExpression = new VariableExpression(argument);
                Optional<SnapshotExpression> optional = result.state().getSnapshot(variableExpression);
                ReferenceExpression value;
                if (optional.isPresent()) {
                    value = modifications.getOrDefault(optional.orElseThrow(), optional.orElseThrow());
                } else {
                    value = variableExpression;
                }
                Optionality optionality = successorState.getOptionality(expression);
                successorState.assign(expression, value);
                successorState.forceOptionality(value, optionality);
            }
        }

        // Check if the result is satisfiable.
        // If it is not satisfiable, this function will not be called.
        if (!(successorState.isSatisfiable())) {
            return null;
        }

        if (result instanceof Result.Exit) {
            return new Result.Exit(successorState);
        }
        if (result instanceof Result.Success) {
            return new Result.Success(successorState, modifications.getOrDefault(variable, variable));
        }
        if (result instanceof Result.Error) {
            return new Result.Error(successorState, modifications.getOrDefault(variable, variable));
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
