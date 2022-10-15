package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a function call expression.
 */
public interface RapidFunctionCallExpression extends RapidExpression {

    /**
     * Returns the argument list containing arguments to be passed to the function call.
     *
     * @return the argument list of this function call.
     */
    @NotNull RapidArgumentList getArgumentList();

    /**
     * Returns the reference expression referencing the function.
     *
     * @return the reference expression of this function call.
     */
    @NotNull RapidReferenceExpression getReferenceExpression();
}
