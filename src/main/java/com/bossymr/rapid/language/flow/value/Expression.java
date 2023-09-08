package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression permits AggregateExpression, BinaryExpression, UnaryExpression, ValueExpression {

    static @NotNull Expression of(boolean value) {
        return new ValueExpression(ConstantValue.of(value));
    }

    static @NotNull Expression of(double value) {
        return new ValueExpression(ConstantValue.of(value));
    }

    static @NotNull Expression of(@NotNull String value) {
        return new ValueExpression(ConstantValue.of(value));
    }

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

}
