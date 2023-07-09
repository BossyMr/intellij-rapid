package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression permits AggregateExpression, BinaryExpression, UnaryExpression, VariableExpression {

    static @NotNull Expression booleanConstant(boolean value) {
        return new VariableExpression(new ConstantValue(RapidType.BOOLEAN, value));
    }

    static @NotNull Expression numericConstant(double value) {
        return new VariableExpression(new ConstantValue(RapidType.NUMBER, value));
    }

    static @NotNull Expression stringConstant(@NotNull String value) {
        return new VariableExpression(new ConstantValue(RapidType.STRING, value));
    }

    void accept(@NotNull ControlFlowVisitor visitor);

}
