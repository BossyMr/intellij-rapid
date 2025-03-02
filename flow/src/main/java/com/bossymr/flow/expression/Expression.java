package com.bossymr.flow.expression;

import com.bossymr.flow.type.ValueType;

import java.util.function.Function;

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

    /**
     * Modify this expression recursively. First, the parent expression is visited, afterward, all children are also
     * visited. The provided function returns an expression which will replace the original expression. If a different
     * expression is returned, it's children are not visited.
     *
     * @param mapper a visitor.
     * @return either this expression or a new expression.
     */
    Expression translate(Function<Expression, Expression> mapper);
}
