package com.bossymr.flow.expression;

import com.bossymr.flow.type.ValueType;

/**
 * An {@code Expression} represents an expression.
 */
public interface Expression {

    /**
     * Returns the return type of this expression.
     *
     * @return the return type of this expression.
     */
    ValueType getType();
}
