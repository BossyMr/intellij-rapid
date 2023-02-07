package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Argument;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.GET;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("elog-message-ev")
public interface EventLogMessageEvent extends EntityModel {

    @GET("{@self}")
    @NotNull NetworkCall<EventLogMessage> getMessage(
            @Nullable @Argument("lang") String languageCode
    );


}
