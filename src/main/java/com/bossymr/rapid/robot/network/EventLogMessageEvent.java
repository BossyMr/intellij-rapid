package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.GET;
import org.jetbrains.annotations.NotNull;

@Entity("elog-message-ev")
public interface EventLogMessageEvent extends EntityModel {

    @GET("{@self}")
    @NotNull Query<EventLogMessage> getMessage();


}
