package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DataFlowPhysicalFunction implements DataFlowFunction {

    private final @NotNull Map<Map<Integer, Constraint>, DataFlowFunctionOutput> output;

    public DataFlowPhysicalFunction() {
        this.output = new HashMap<>();
    }

    public void putOutput(@NotNull Map<Integer, Constraint> constraints, @NotNull DataFlowFunctionOutput output) {
        this.output.put(constraints, output);
    }

    @Override
    public @NotNull DataFlowFunctionOutput getOutput(@NotNull DataFlowBlock block, @NotNull Map<Integer, Value> arguments) {
        Map<Integer, Constraint> constraints = new HashMap<>();
        arguments.forEach((index, value) -> {
            Constraint constraint = block.getConstraint(value);
            constraints.put(index, constraint);
        });
        return output.get(constraints);
    }
}
