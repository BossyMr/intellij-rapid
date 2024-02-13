package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.annotations.Argument;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Fetch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("elog-message-ev")
public interface EventLogMessageEvent {

    @Fetch("{@self}")
    @NotNull NetworkQuery<EventLogMessage> getMessage(@Nullable @Argument("lang") String languageCode);


}
