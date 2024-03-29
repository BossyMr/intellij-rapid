package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Entity({"elog-domain", "elog-domain-li"})
public interface EventLogCategory {

    @Property("domain-name")
    @Nullable String getName();

    @Property("numevts")
    @Nullable Integer getMessageCount();

    @Property("buffsize")
    @Nullable Integer getBufferSize();

    /**
     * Returns all messages in this event log category.
     *
     * @return all messages in this event log category.
     */
    @Fetch("{@self}")
    @NotNull NetworkQuery<List<EventLogMessage>> getMessages();

    /**
     * Returns all messages in this event log category, starting with the specified message.
     *
     * @param message the identifier of the first message.
     * @return all messages in this event log category, starting with the specified message.
     */
    @Fetch("{@self}")
    @NotNull NetworkQuery<List<EventLogMessage>> getMessages(@Argument("elogseqnum") int message);

    /**
     * Returns the message in this category with the specified sequence identifier.
     *
     * @param message the message sequence identifier.
     * @return the message in this category with the specified sequence identifier.
     */
    @Fetch("{@self}/{message}")
    @NotNull NetworkQuery<EventLogMessage> getMessage(@Path("message") int message);

    /**
     * Clears all messages in this category.
     */
    @Fetch(method = FetchMethod.POST, value = "{@self}?action=clear")
    @NotNull NetworkQuery<Void> clear();

    /**
     * Subscribes to new events in this category.
     */
    @Subscribable("{@self}")
    @NotNull SubscribableNetworkQuery<EventLogMessageEvent> onMessage();

}
