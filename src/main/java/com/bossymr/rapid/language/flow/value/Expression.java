package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression permits AggregateExpression, BinaryExpression, UnaryExpression, ValueExpression {

    static @NotNull Expression of(boolean value) {
        return new ValueExpression(new ConstantValue(RapidType.BOOLEAN, value));
    }

    static @NotNull Expression of(double value) {
        return new ValueExpression(new ConstantValue(RapidType.NUMBER, value));
    }

    static @NotNull Expression of(@NotNull String value) {
        return new ValueExpression(new ConstantValue(RapidType.STRING, value));
    }

    void accept(@NotNull ControlFlowVisitor visitor);

}
