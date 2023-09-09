package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code RapidStructure} is a symbol which declares a type.
 */
public interface RapidStructure extends RapidSymbol, RapidVisibleSymbol {

    /**
     * Create a new type of this structure.
     *
     * @return a new type of this structure.
     */
    default @NotNull RapidType createType() {
        return new RapidType(this);
    }

    /**
     * Creates a new array with the specified dimensions of this structure.
     *
     * @param dimensions the dimensions.
     * @return a new type of this structure.
     */
    default @NotNull RapidType createType(int dimensions) {
        return new RapidType(this, dimensions);
    }

}
