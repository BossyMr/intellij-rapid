package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.value.ConstantExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;

public enum HardcodedContract {
    PRESENT(new ContractBuilder("Present", RapidPrimitiveType.BOOLEAN)
            .withArgumentGroup(false).withArgument(ParameterType.REFERENCE, "OptPar", RapidPrimitiveType.ANYTYPE).build()
            .withResult()
            .withCondition((state, variables) -> state.assign(variables.getArgument("OptPar"), variables.getArgument("OptPar")))
            .withCondition((state, variables) -> state.forceOptionality(variables.getArgument("OptPar"), Optionality.PRESENT))
            .withCondition((state, variables) -> state.assign(variables.getOutput(), new ConstantExpression(RapidPrimitiveType.BOOLEAN, true)))
            .withSuccess()
            .withResult()
            .withCondition((state, variables) -> state.assign(variables.getArgument("OptPar"), variables.getArgument("OptPar")))
            .withCondition((state, variables) -> state.forceOptionality(variables.getArgument("OptPar"), Optionality.MISSING))
            .withCondition((state, variables) -> state.assign(variables.getOutput(), new ConstantExpression(RapidPrimitiveType.BOOLEAN, false)))
            .withSuccess()
            .build());

    private final @NotNull DataFlowFunction function;

    HardcodedContract(@NotNull DataFlowFunction function) {
        this.function = function;
    }

    public @NotNull DataFlowFunction getFunction() {
        return function;
    }
}
