package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a test statement.
 */
public interface RapidTestStatement extends RapidStatement {

    /**
     * Returns the expression which is evaluated by this statement.
     *
     * @return the expression, or {@code null} if this statement is incomplete.
     */
    @Nullable RapidExpression getExpression();

    /**
     * Returns the test case statements of this test statement.
     *
     * @return a list of test case statements.
     */
    @NotNull List<RapidTestCaseStatement> getTestCaseStatements();

}
