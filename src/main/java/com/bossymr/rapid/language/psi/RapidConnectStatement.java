package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a connect statement.
 */
public interface RapidConnectStatement extends RapidStatement {

    /**
     * Returns the expression which is to be connected.
     *
     * @return the expression to the left of this statement.
     */
    @Nullable RapidExpression getLeft();

    /**
     * Returns the expression which should reference a trap routine which the left expression is connected with.
     *
     * @return the expression to the right of this statement.
     */
    @Nullable RapidExpression getRight();

}
