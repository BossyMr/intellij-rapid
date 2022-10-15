package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an alias.
 */
public interface RapidAlias extends RapidStructure {

    /**
     * Returns the type referenced by this alias.
     *
     * @return the type referenced by this alias.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element of this alias, or {@code null} if the alias is incomplete.
     *
     * @return the type element of this alias.
     */
    @Nullable RapidTypeElement getTypeElement();

}