package io.github.bossymr.language.psi;

import com.intellij.psi.PsiElement;

import java.util.List;

/**
 * Represents a backward clause.
 */
public interface RapidBackwardClause extends PsiElement {

    /**
     * Returns a list of statements declared in this clause.
     *
     * @return a list of statements in this clause.
     */
    List<RapidStatement> getStatements();

}
