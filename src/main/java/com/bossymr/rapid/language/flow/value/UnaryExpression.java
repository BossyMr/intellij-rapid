package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code UnaryExpression} expression performs the specified operation on the specified value.
 *
 * @param operator the operator.
 * @param value the value.
 */
public record UnaryExpression(@NotNull UnaryOperator operator, @NotNull Value value) implements Expression {
    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return 
        visitor.visitUnaryExpression(this);
    }
}
