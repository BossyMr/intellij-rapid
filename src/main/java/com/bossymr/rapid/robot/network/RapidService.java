package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Service("/rw/rapid")
public interface RapidService {

    @NotNull ExecutionService getExecutionService();

    @NotNull TaskService getTaskService();

    /**
     * Searches for symbols on this robot, according to the specified arguments. To build a correctly formatted argument
     * map, use {@link SymbolQueryBuilder}.
     *
     * @param fields the NetworkCall arguments.
     * @return the symbols on this robot.
     */
    @POST("/symbols?action=search-symbols")
    @NotNull NetworkCall<List<Symbol>> findSymbols(
            @Field Map<String, String> fields
    );

    /**
     * Returns the symbol with the specified path. If a symbol with the specified path does not exist, a
     * {@link com.bossymr.network.ResponseStatusException} will be thrown, with the status code 400.
     *
     * @param symbol the path of the symbol.
     * @return the symbol.
     */
    @GET("/symbol/properties/{symbol}")
    @NotNull NetworkCall<Symbol> findSymbol(
            @Path("symbol") String symbol
    );
}
