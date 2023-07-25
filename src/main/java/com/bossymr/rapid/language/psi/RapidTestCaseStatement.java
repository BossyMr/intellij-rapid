package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a test case statement in a test statement.
 */
public interface RapidTestCaseStatement extends RapidStatement {

    /**
     * Checks if the element represents a default case statement.
     *
     * @return if the element is a default case statement.
     */
    boolean isDefault();

    /**
     * Returns the expression list of this case statement.
     *
     * @return the expression list of this case statement.
     */
    @Nullable List<RapidExpression> getExpressions();

    @Nullable RapidExpressionList getExpressionList();

    /**
     * Returns the statements in this test case statement.
     *
     * @return the statements in this test case statement.
     */
    @NotNull RapidStatementList getStatements();

}
