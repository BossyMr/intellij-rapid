package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Property;
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
    @GET("/rw/rapid/symbol/data/{#symburl}")
    @NotNull NetworkCall<SymbolValue> getValue();

    @Property("ndim")
    int getSize();
}
