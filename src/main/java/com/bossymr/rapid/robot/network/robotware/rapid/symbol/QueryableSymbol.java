package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code SymbolModel} with a queryable value.
 */
public interface QueryableSymbol extends SymbolModel, TypeSymbol {

    /**
     * Retrieves the string value of the symbol.
     *
     * @return the string value of the symbol.
     */
    @Fetch("/rw/rapid/symbol/data/{#symburl}")
    @NotNull NetworkQuery<SymbolValue> getValue();

    @Property("ndim")
    int getSize();
}
