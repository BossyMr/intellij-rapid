package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an aggregate expression.
 */
public interface RapidAggregateExpression extends RapidExpression {

    /**
     * Returns the expressions in this aggregate expression.
     *
     * @return the expressions in this aggregate expression.
     */
    @NotNull List<RapidExpression> getExpressions();
}
