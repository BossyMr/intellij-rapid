package com.bossymr.rapid.language.flow.data.virtual;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantValue;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.bossymr.rapid.language.flow.data.virtual.HardcodedFunctionBuilder.create;

public enum HardcodedFunctions {
    ABS(create("Abs", RapidType.NUMBER)
            .withArgumentGroup(false).withArgument("Value", RapidType.NUMBER, ParameterType.INPUT).build()
            .withResult(Map.of(0, new NumericConstraint(Optionality.PRESENT, NumericConstraint.Bound.MIN_VALUE, new NumericConstraint.Bound(false, 0))),
                    (returnValue, arguments) -> new Condition(returnValue, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.MULTIPLY, arguments.get(0), new ConstantValue(RapidType.NUMBER, -1))))
            .withResult(Map.of(0, new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(true, 0), NumericConstraint.Bound.MAX_VALUE)),
                    (returnValue, arguments) -> new Condition(returnValue, ConditionType.EQUALITY, new VariableExpression(arguments.get(0))))
            .build());

    private final @NotNull DataFlowFunction function;

    HardcodedFunctions(@NotNull DataFlowFunction function) {
        this.function = function;
    }

    public @NotNull DataFlowFunction getFunction() {
        return function;
    }
}
