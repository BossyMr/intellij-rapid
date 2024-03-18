package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Argument;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.client.FetchMethod;
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
        @Fetch("?resource=count")
  @NotNull NetworkQuery<List<EventLogCategory>> getCategories(@Nullable @Argument("lang") String languageCode);

    /**
     * Clears all messages in this event log.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=clearall")
  @NotNull NetworkQuery<Void> clearAll();
}
