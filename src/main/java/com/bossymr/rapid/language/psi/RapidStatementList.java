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
    Attribute getAttribute();

    /**
     * Returns the expression list containing the errors which are handled by this error clause.
     *
     * @return the expression list, or {@code null} if this statement list is not an error clause.
     */
    @Nullable RapidExpressionList getExpressionList();

    /**
     * Returns the statements contained in this statement list.
     *
     * @return the statements contained in this statement list.
     */
    List<RapidStatement> getStatements();

    /**
     * Represents the different attributes of a statement list.
     */
    enum Attribute {
        /**
         * This statement list is a regular statement list.
         */
        STATEMENT_LIST,

        /**
         * This statement list is an error clause, and {@link #getExpressionList()} can be used to get the errors which
         * are handled.
         */
        ERROR_CLAUSE,

        /**
         * This statement list is an undo clause.
         */
        UNDO_CLAUSE,

        /**
         * This statement list is a backward clause.
         */
        BACKWARD_CLAUSE
    }
}
