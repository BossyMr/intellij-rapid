package com.bossymr.rapid.language.psi;

/**
 * Represents the different attributes of a statement list.
 */
public enum StatementListType {

    /**
     * This statement list is a regular statement list.
     */
    STATEMENT_LIST,

    /**
     * This statement list is an error clause.
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
