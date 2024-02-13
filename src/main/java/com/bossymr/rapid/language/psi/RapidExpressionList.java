package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an expression list.
 */
public interface RapidExpressionList extends RapidElement {

    /**
     * Returns the expressions contained in this expression list.
     *
     * @return the expressions contained in this expression list.
     */
    @NotNull List<RapidExpression> getExpressions();

}
