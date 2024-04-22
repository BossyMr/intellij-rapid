package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Argument;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("elog-message-ev")
public interface EventLogMessageEvent {

    @Fetch("{@self}")
    @NotNull NetworkQuery<EventLogMessage> getMessage(@Nullable @Argument("lang") String languageCode);


}
