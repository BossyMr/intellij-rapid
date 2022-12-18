package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Argument;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("elog-message-ev")
public interface EventLogMessageEvent extends EntityModel {

    @GET("{@self}")
    @NotNull Query<EventLogMessage> getMessage(
            @Nullable @Argument("lang") String languageCode
    );


}
