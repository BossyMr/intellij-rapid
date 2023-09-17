package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractDataFlowFunction implements DataFlowFunction {

    protected abstract @NotNull Set<DataFlowFunction.Result> getResults();

    @Override
    public @NotNull Set<Result> getOutput(@NotNull DataFlowState state, @NotNull BranchingInstruction.CallInstruction instruction) {
        Set<DataFlowFunction.Result> output = new HashSet<>();
        for (Result result : getResults()) {
            // TODO: 2023-09-17 The snapshots for parameters aren't being forwarded to the snapshots for the arguments passed to the function (if it isn't an INPUT parameter)
            Map<ReferenceExpression, ReferenceExpression> variableMap = new HashMap<>();
            if (result.variable() != null && instruction.returnValue() != null) {
                variableMap.put(result.variable(), instruction.returnValue());
            }
            Map<Argument, ReferenceExpression> arguments = getArguments(getBlock(), instruction.arguments());
            for (Argument argument : arguments.keySet()) {
                ReferenceExpression expression = arguments.get(argument);
                variableMap.put(new VariableExpression(argument), expression);
            }
            DataFlowState successorState = DataFlowState.copy(result.state(), state)
                    .modify(variableMap);
            if (!(successorState.isSatisfiable())) {
                continue;
            }
            if (result instanceof Result.Exit) {
                output.add(new Result.Exit(successorState));
            }
            if (result instanceof Result.Success) {
                output.add(new Result.Success(successorState, variableMap.getOrDefault(result.variable(), result.variable())));
            }
            if (result instanceof Result.Error) {
                output.add(new Result.Success(successorState, variableMap.getOrDefault(result.variable(), result.variable())));
            }
        }
        return output;
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
