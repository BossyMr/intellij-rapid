package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.AsynchronousQuery;
import com.bossymr.rapid.robot.network.query.AsynchronousQuery.Asynchronous;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A service used to fetch the robots event log.
 */
@Service("/rw/elog")
public interface EventLogService {

    /**
     * Returns all categories in this event log.
     *
     * @param languageCode the language to localize the category name, or {@code null} to not provide the category
     * name.
     * @return all categories in this event log.
     */
    @GET("?resource=count")
    @NotNull Query<List<EventLogCategory>> getCategories(
            @Nullable @Argument("lang") String languageCode
    );

    /**
     * Clears all messages in this event log.
     */
    @POST("?action=clearall")
    @NotNull Query<Void> clearAll();

    /**
     * Dumps the event log into a file with the specified path.
     *
     * @param path the path, which can include environment variables.
     */
    @Asynchronous("?action=saveraw")
    @NotNull AsynchronousQuery dump(
            @NotNull @Field("path") String path
    );
}
