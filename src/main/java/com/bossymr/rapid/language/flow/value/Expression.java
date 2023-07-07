package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression permits AggregateExpression, BinaryExpression, UnaryExpression, VariableExpression {

    <T> T accept(@NotNull ControlFlowVisitor<T> visitor);

}
