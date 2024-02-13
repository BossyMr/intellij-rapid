package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a while statement.
 */
public interface RapidWhileStatement extends RapidStatement {

    /**
     * Returns the condition of this loop.
     *
     * @return the condition of this loop, or {@code null} if this statement is incomplete.
     */
    @Nullable RapidExpression getCondition();

    /**
     * Returns the statement list of this loop.
     *
     * @return the statement list, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidStatementList getStatementList();

}
