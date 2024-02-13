package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidVariable} represents a variable with a value.
 */
public interface RapidVariable extends RapidSymbol {

    /**
     * Returns the type of this variable.
     *
     * @return the type of this variable, or {@code null} if this symbol is incomplete.
     */
    @Nullable RapidType getType();

    /**
     * Checks if this variable can be modified.
     *
     * @return if this variable can be modified.
     */
    default boolean isModifiable() {
        return true;
    }

}
