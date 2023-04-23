package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a list of statements.
 */
public interface RapidStatementList extends RapidElement {

    /**
     * Returns the attribute of this statement list.
     *
     * @return the attribute of this statement list.
     */
    StatementListType getAttribute();

    /**
     * Returns the expression list containing the errors which are handled by this error clause.
     *
     * @return the expression list, or {@code null} if this statement list is not an error clause.
     */
    @Nullable List<RapidExpression> getExpressions();

    /**
     * Returns the statements contained in this statement list.
     *
     * @return the statements contained in this statement list.
     */
    List<RapidStatement> getStatements();

}
