package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an if statement.
 */
public interface RapidIfStatement extends RapidStatement {

    /**
     * Returns the expression which is evaluated by this statement.
     *
     * @return the expression, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidExpression getCondition();

    /**
     * Returns the statements to be executed if the condition is {@code true}.
     *
     * @return the statements to be executed, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidStatementList getThenBranch();

    /**
     * Returns the statements to be executed if the condition is {@code true}.
     *
     * @return the statements to be executed, or {@code null} if the statement is incomplete or
     * has no {@code else} statement list.
     */
    @Nullable RapidStatementList getElseBranch();
}
