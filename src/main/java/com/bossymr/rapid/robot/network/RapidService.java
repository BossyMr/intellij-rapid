package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.Query;
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
     * @param fields the query arguments.
     * @return the symbols on this robot.
     */
    @POST("/symbols?action=search-symbols")
    @NotNull Query<List<SymbolState>> findSymbols(
            @FieldMap Map<String, String> fields
    );

    /**
     * Returns the symbol with the specified path. If a symbol with the specified path does not exist, a
     * {@link ResponseStatusException} will be thrown, with the status code 400.
     *
     * @param symbol the path of the symbol.
     * @return the symbol.
     */
    @GET("/symbol/properties/{symbol}")
    @NotNull Query<SymbolState> findSymbol(
            @Path("symbol") String symbol
    );
}
