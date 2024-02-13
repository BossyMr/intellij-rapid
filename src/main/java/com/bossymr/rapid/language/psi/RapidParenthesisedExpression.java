package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

public interface RapidParenthesisedExpression extends RapidExpression {

    /**
     * Returns the expression in between the parentheses.
     * @return the expression inside this parenthesized expression.
     */
    @Nullable RapidExpression getExpression();

}
