package com.bossymr.rapid.language.flow.conditon;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An {@code Expression} represents an expression.
 */
public sealed interface Expression {

    record Variable(@NotNull Value value) implements Expression {}

    record Index(@NotNull Value.Variable variable, @NotNull Value index) implements Expression {}

    record Aggregate(@NotNull List<Value> values) implements Expression {}

    /**
     * A {@code Binary} expression performs the specified operation on the specified values.
     *
     * @param operator the operator.
     * @param left the first value.
     * @param right the second value.
     */
    record Binary(@NotNull Operator.BinaryOperator operator, @NotNull Value left, @NotNull Value right) implements Expression {}

    /**
     * A {@code Unary} expression performs the specified operation on the specified value.
     *
     * @param operator the operator.
     * @param value the value.
     */
    record Unary(@NotNull Operator.UnaryOperator operator, @NotNull Value value) implements Expression {}

}
