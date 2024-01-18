package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a list of statements.
 */
public interface RapidStatementList extends RapidElement {

    static @Nullable RapidStatementList getStatementList(@NotNull PsiElement element) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(element);
        if (routine == null) {
            return null;
        }
        return routine.getStatementLists().stream()
                      .filter(list -> PsiTreeUtil.isAncestor(list, element, false))
                      .findFirst().orElseThrow();
    }

    /**
     * Returns the attribute of this statement list.
     *
     * @return the attribute of this statement list.
     */
    BlockType getStatementListType();

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

    List<PhysicalField> getFields();

}
