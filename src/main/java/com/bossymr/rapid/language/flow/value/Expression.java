package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression {

    void accept(@NotNull ControlFlowVisitor visitor);

    record Variable(@NotNull Value value) implements Expression {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitVariableExpression(this);
        }
    }

    record Aggregate(@NotNull List<Value> values) implements Expression {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitAggregateExpression(this);
        }
    }

    /**
     * A {@code Binary} expression performs the specified operation on the specified values.
     *
     * @param operator the operator.
     * @param left the first value.
     * @param right the second value.
     */
    record Binary(@NotNull Operator.BinaryOperator operator, @NotNull Value left, @NotNull Value right) implements Expression {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitBinaryExpression(this);
        }
    }

    /**
     * A {@code Unary} expression performs the specified operation on the specified value.
     *
     * @param operator the operator.
     * @param value the value.
     */
    record Unary(@NotNull Operator.UnaryOperator operator, @NotNull Value value) implements Expression {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitUnaryExpression(this);
        }
    }

}
