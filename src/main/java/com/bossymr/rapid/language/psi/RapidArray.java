package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidArray extends RapidElement {

    /**
     * Returns the expressions in this array.
     *
     * @return the expressions in this array.
     */
    @NotNull List<RapidExpression> getDimensions();

}
