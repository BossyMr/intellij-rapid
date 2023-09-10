package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractDataFlowFunction implements DataFlowFunction {

    protected abstract @NotNull Map<Map<Argument, Constraint>, Set<DataFlowFunction.Result>> getResults();

    @Override
    public @NotNull Set<Result> getOutput(@NotNull DataFlowBlock callingBlock, @NotNull Map<Argument, Constraint> arguments) {
        Map<Map<Argument, Constraint>, Set<Result>> results = getResults();
        return results.keySet().stream()
                .filter(constraints -> contains(constraints, arguments))
                .flatMap(constraints -> results.get(constraints).stream())
                .collect(Collectors.toSet());
    }

    private boolean contains(@NotNull Map<Argument, Constraint> results, @NotNull Map<Argument, Constraint> arguments) {
        for (Argument argument : results.keySet()) {
            Constraint constraint = results.get(argument);
            Constraint argumentConstraint = arguments.containsKey(argument) ? arguments.get(argument) : Constraint.any(argument.getType(), Optionality.MISSING);
            if (!(constraint.intersects(argumentConstraint))) {
                return false;
            }
        }
        return true;
    }
}
