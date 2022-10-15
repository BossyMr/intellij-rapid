package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a variable.
 */
public interface RapidVariable extends RapidSymbol {

    /**
     * Returns the type of this parameter.
     *
     * @return the type of this parameter, or {@code null} if the variable is incomplete.
     */
    @Nullable RapidType getType();


    /**
     * Returns the type element of this parameter.
     *
     * @return the type element of this parameter, or {@code null} if the parameter is incomplete.
     */
    @Nullable RapidTypeElement getTypeElement();

}
