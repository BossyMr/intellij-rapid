package com.bossymr.rapid.language.flow.conditon;

import com.bossymr.rapid.language.flow.conditon.Expression.Binary;
import com.bossymr.rapid.language.flow.conditon.Expression.Unary;

/**
 * An {@code Operator} represents an operation which can be performed.
 */
public sealed interface Operator {

    /**
     * A {@code BinaryOperator} represents an operation which can be performed in a {@link Binary}.
     */
    enum BinaryOperator implements Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, LESS_THAN, EQUAL_TO, GREATER_THAN, AND, XOR, OR
    }

    /**
     * A {@code UnaryOperator} represents an operation which can be performed in a {@link Unary}.
     */
    enum UnaryOperator implements Operator {
        NOT, NEGATE
    }
}
