package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code BinaryExpression} expression performs the specified operation on the specified values.
 *
 * @param operator the operator.
 * @param left the first value.
 * @param right the second value.
 */
public record BinaryExpression(@NotNull BinaryOperator operator, @NotNull Value left, @NotNull Value right) implements Expression {
    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}
