package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.FieldMap;
import com.bossymr.rapid.robot.network.query.Query.GET;
import com.bossymr.rapid.robot.network.query.Query.POST;
import com.bossymr.rapid.robot.network.query.Query.Path;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Service("/rw/rapid")
public interface RapidService {

    @NotNull ExecutionService getExecutionService();

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
     * {@link com.bossymr.rapid.robot.ResponseStatusException} will be thrown, with the status code 400.
     *
     * @param symbol the path of the symbol.
     * @return the symbol.
     * @throws ResponseStatusException if a symbol with the specified path does not exist.
     */
    @GET("/symbol/properties/{symbol}")
    @NotNull Query<SymbolState> findSymbol(
            @Path("symbol") String symbol
    ) throws ResponseStatusException;


}
