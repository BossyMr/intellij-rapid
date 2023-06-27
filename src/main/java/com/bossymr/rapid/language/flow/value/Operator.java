package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.value.Expression.Binary;
import com.bossymr.rapid.language.flow.value.Expression.Unary;

/**
 * An {@code Operator} represents an operation which can be performed.
 */
public sealed interface Operator {

    /**
     * A {@code BinaryOperator} represents an operation which can be performed in a {@link Binary}.
     */
    enum BinaryOperator implements Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL_TO, NOT_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL, AND, XOR, OR
    }

    /**
     * A {@code UnaryOperator} represents an operation which can be performed in a {@link Unary}.
     */
    enum UnaryOperator implements Operator {
        NOT, NEGATE
    }
}
