package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Entity({"elog-domain", "elog-domain-li"})
public interface EventLogCategory extends EntityModel {

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
    @GET("{@self}")
    @NotNull Query<List<EventLogMessage>> getMessages();

    /**
     * Returns all messages in this event log category, starting with the specified message.
     *
     * @param message the identifier of the first message.
     * @return all messages in this event log category, starting with the specified message.
     */
    @GET("{@self}")
    @NotNull Query<List<EventLogMessage>> getMessages(
            @Argument("elogseqnum") int message
    );

    /**
     * Returns the message in this category with the specified sequence identifier.
     *
     * @param message the message sequence identifier.
     * @return the message in this category with the specified sequence identifier.
     */
    @GET("{@self}/{message}")
    @NotNull Query<EventLogMessage> getMessage(
            @Path("message") int message
    );

    /**
     * Clears all messages in this category.
     */
    @POST("{@self}?action=clear")
    @NotNull Query<Void> clear();

    /**
     * Subscribes to new events in this category.
     */
    @Subscribable("{@self}")
    @NotNull SubscribableQuery<EventLogMessageEvent> onMessage();

}
