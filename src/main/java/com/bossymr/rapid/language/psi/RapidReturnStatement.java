package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a return statement.
 */
public interface RapidReturnStatement extends RapidStatement {

    /**
     * Returns the expression which is returned by this statement.
     *
     * @return the expression which is returned by this statement, or {@code null} if no expression is returned.
     */
    @Nullable RapidExpression getExpression();

}
