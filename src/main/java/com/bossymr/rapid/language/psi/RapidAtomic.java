package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an atomic structure.
 */
public interface RapidAtomic extends RapidStructure {

    /**
     * Returns the value structure type that can be used as an alternative to this atomic type in a value context, for
     * example in assignment and references in expressions.
     *
     * @return the associated type of this structure.
     */
    @Nullable RapidType getType();
}
