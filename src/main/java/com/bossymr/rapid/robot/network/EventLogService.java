package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Argument;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Service;
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
    @NotNull NetworkCall<List<EventLogCategory>> getCategories(
            @Nullable @Argument("lang") String languageCode
    );

    /**
     * Clears all messages in this event log.
     */
    @POST("?action=clearall")
    @NotNull NetworkCall<Void> clearAll();
}
