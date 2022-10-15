package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a component inside a record.
 *
 * @see RapidRecord#getComponents()
 */
public interface RapidComponent extends RapidSymbol {

    /**
     * Returns the type of this component.
     *
     * @return the type of this component.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element of this component.
     *
     * @return the type element of this component.
     */
    @Nullable RapidTypeElement getTypeElement();

}
