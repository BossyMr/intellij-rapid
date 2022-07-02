package io.github.bossymr.language.psi;

import com.intellij.psi.PsiElement;

import java.util.List;

/**
 * Represents an error clause.
 */
public interface RapidExceptionClause extends PsiElement {

    /**
     * Returns a list of the errors which are caught by this clause.
     *
     * @return a list of expressions.
     */
    List<RapidExpression> getExceptions();

    /**
     * Returns a list of statements declared in this clause.
     *
     * @return a list of statements in this clause.
     */
    List<RapidStatement> getStatements();

}
