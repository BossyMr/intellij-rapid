package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Entity("elog-message")
public interface EventLogMessage {

    @Property("msgtype")
    @NotNull EventLogMessageType getMessageType();

    @Property("code")
    int getMessageCode();

    @Property("tstamp")
    @NotNull LocalDateTime getTimestamp();

    @Property("title")
    @Nullable String getMessageTitle();

    @Property("desc")
    @Nullable String getDescription();

    @Property("conseqs")
    @Nullable String getConsequences();

    @Property("causes")
    @Nullable String getCauses();

    @Property("actions")
    @Nullable String getActions();

}
