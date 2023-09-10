package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.value.ValueExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;

public enum HardcodedContract {
    PRESENT(new ContractBuilder("Present", RapidPrimitiveType.BOOLEAN)
            .withArgumentGroup(false).withArgument(ParameterType.REFERENCE, "OptPar", RapidPrimitiveType.ANYTYPE).build()
            .withResult()
            .withCondition((state, map) -> state.assign(new Condition(map.getArgument("OptPar"), ConditionType.EQUALITY, new ValueExpression(map.getArgument("OptPar"))), false))
            .whereArgument("OptPar", Constraint.any(RapidPrimitiveType.ANYTYPE, Optionality.PRESENT))
            .withSuccess(BooleanConstraint.alwaysTrue())
            .withResult()
            .withCondition((state, map) -> state.assign(new Condition(map.getArgument("OptPar"), ConditionType.EQUALITY, new ValueExpression(map.getArgument("OptPar"))), false))
            .whereArgument("OptPar", Constraint.any(RapidPrimitiveType.ANYTYPE, Optionality.MISSING))
            .withSuccess(BooleanConstraint.alwaysFalse())
            .build());

    private final @NotNull DataFlowFunction function;

    HardcodedContract(@NotNull DataFlowFunction function) {
        this.function = function;
    }

    public @NotNull DataFlowFunction getFunction() {
        return function;
    }
}
