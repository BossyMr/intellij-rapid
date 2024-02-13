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
    @NotNull RapidType createType();

    default @NotNull RapidType createType(int dimensions) {
        return createType().createArrayType(dimensions);
    }

}
