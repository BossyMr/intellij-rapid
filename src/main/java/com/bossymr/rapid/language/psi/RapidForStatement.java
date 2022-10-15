package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a for statement.
 */
public interface RapidForStatement extends RapidStatement {

    /**
     * Returns the variable representing the index of this for statement.
     *
     * @return the index variable, or {@code null} if this statement is incomplete.
     */
    @Nullable RapidTargetVariable getVariable();

    /**
     * Returns the expression representing the start value of this for loop.
     *
     * @return the expression, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidExpression getFromExpression();

    /**
     * Returns the expression representing the end value of this for loop.
     *
     * @return the expression, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidExpression getToExpression();

    /**
     * Returns the expression representing the difference to the index on each loop.
     *
     * @return the expression, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidExpression getStepExpression();

    /**
     * Returns the statement list of this statement.
     *
     * @return the statement list of this statement.
     */
    @Nullable RapidStatementList getStatementList();

}
