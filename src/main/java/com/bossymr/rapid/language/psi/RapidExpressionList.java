package com.bossymr.rapid.language.psi;

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
    List<RapidExpression> getExpressions();

}
