package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity({"elog-message", "elog-message-li"})
public interface EventLogMessage extends EntityModel {

    @Property("msg-type")
    int getMessageType();

    @Property("code")
    int getMessageCode();

    @Property("src-name")
    @NotNull String getSource();

    @ApiStatus.Internal
    @Property("tstamp")
    @NotNull String getInternalLocalDateTime();

    default @NotNull LocalDateTime getLocalDateTime() {
        String value = getInternalLocalDateTime().replaceAll(" ", "");
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

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
