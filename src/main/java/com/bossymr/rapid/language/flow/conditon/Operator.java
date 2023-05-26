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
        ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO,
        GREATER_THAN, NOT_EQUAL_TO, AND, EXLUSIVE_OR, OR, NOT
    }

    /**
     * A {@code UnaryOperator} represents an operation which can be performed in a {@link Unary}.
     */
    enum UnaryOperator implements Operator {
        NOT, NEGATE
    }
}
