package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an argument in a function or procedure call.
 */
public interface RapidArgument extends RapidElement {

    /**
     * Returns the name of the parameter this argument declares.
     *
     * @return the name of the parameter this argument declares.
     */
    @Nullable RapidReferenceExpression getParameter();

    /**
     * Returns the expression of this argument.
     *
     * @return the expression of this argument.
     */
    @Nullable RapidExpression getArgument();
}
