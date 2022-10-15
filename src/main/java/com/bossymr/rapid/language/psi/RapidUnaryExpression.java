package com.bossymr.rapid.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a unary, or prefix, expression.
 */
public interface RapidUnaryExpression extends RapidExpression {

    /**
     * Returns the expression of this expression.
     *
     * @return the expression of this expression, or {@code null} if the expression is incomplete.
     */
    @Nullable RapidExpression getExpression();

    /**
     * Returns the element representing the operation sign of this expression.
     *
     * @return the operation sign of this expression.
     */
    @NotNull PsiElement getSign();

}
