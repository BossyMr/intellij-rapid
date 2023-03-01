package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidExpressionCodeFragment} represents a code fragment with a single expression.
 */
public interface RapidExpressionCodeFragment extends RapidCodeFragment {

    /**
     * Returns the expression in this fragment.
     *
     * @return the expression in this fragment.
     */
    @Nullable RapidExpression getExpression();

}
