package com.bossymr.rapid.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a binary expression.
 */
public interface RapidBinaryExpression extends RapidExpression {

    /**
     * Returns the left expression of the expression.
     *
     * @return the left expression.
     */
    @NotNull RapidExpression getLeft();

    /**
     * Returns the right expression of this expression.
     *
     * @return the right expression of this expression, or {@code null} if this expression is incomplete.
     */
    @Nullable RapidExpression getRight();

    /**
     * Returns the element representing the operation sign of this binary expression.
     *
     * @return the operation sign of this expression.
     */
    PsiElement getSign();

}
