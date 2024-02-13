package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

public interface RapidIndexExpression extends RapidExpression {

    /**
     * Returns the expression specifying the array to access.
     *
     * @return the expression.
     */
    @NotNull RapidExpression getExpression();

    /**
     * Returns the array specifying the value to access in the array.
     *
     * @return the array of this expression.
     */
    @NotNull RapidArray getArray();

}
