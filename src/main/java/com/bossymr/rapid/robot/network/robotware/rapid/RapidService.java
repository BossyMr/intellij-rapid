package com.bossymr.rapid.robot.network.robotware.rapid;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolQuery;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A {@code Service} used to communicate with the {@code RAPID} section of a robot.
 */
@Service("/rw/rapid")
public interface RapidService {

    /**
     * Returns the execution service.
     *
     * @return the execution service.
     */
    @NotNull ExecutionService getExecutionService();

    /**
     * Returns the task service.
     *
     * @return the task service.
     */
    @NotNull TaskService getTaskService();

    /**
     * Searches for symbols on this robot, according to the specified arguments. SymbolQueryBuilder To build a correctly
     * formatted argument map, use {@link SymbolQuery}.
     *
     * @param fields the NetworkCall arguments.
     * @return the symbols on this robot.
     */
    @POST(value = "/symbols", arguments = "action=search-symbols")
    @NotNull NetworkCall<List<SymbolState>> findSymbols(
            @Field Map<String, String> fields
    );

    /**
     * Returns the symbol with the specified path.
     *
     * @param symbol the path of the symbol.
     * @return the symbol.
     */
    @GET("/symbol/properties/{symbol}")
    @NotNull NetworkCall<SymbolState> findSymbol(
            @Path("symbol") String symbol
    );
}
