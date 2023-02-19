package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.GET;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code SymbolState} with a queryable value.
 */
public interface QueryableSymbolState extends SymbolState {

    /**
     * Retrieves the string value of the symbol.
     *
     * @return the string value of the symbol.
     */
    @GET("{@data}")
    @NotNull NetworkCall<SymbolValue> getValue();

}
