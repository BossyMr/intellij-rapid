package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.value.ValueExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public enum HardcodedContract {
    PRESENT(new ContractBuilder("Present", RapidType.BOOLEAN)
            .withArgumentGroup(false).withArgument(ParameterType.REFERENCE, "OptPar", RapidType.ANYTYPE).build()
            .withResult()
            .withCondition((state, map) -> state.assign(new Condition(map.getArgument("OptPar"), ConditionType.EQUALITY, new ValueExpression(map.getArgument("OptPar"))), false))
            .whereArgument("OptPar", Constraint.any(RapidType.ANYTYPE, Optionality.PRESENT))
            .withSuccess(BooleanConstraint.alwaysTrue())
            .withResult()
            .withCondition((state, map) -> state.assign(new Condition(map.getArgument("OptPar"), ConditionType.EQUALITY, new ValueExpression(map.getArgument("OptPar"))), false))
            .whereArgument("OptPar", Constraint.any(RapidType.ANYTYPE, Optionality.MISSING))
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
