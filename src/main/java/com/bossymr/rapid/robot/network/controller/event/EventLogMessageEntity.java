package com.bossymr.rapid.robot.network.controller.event;

import com.bossymr.rapid.robot.network.client.annotations.Entity;
import com.bossymr.rapid.robot.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity({"elog-message-li", "elog-message"})
public record EventLogMessageEntity(@Field("msgtype") int messageType,
                                    @Field("code") int code,
                                    @NotNull @Field("tstamp") String timestamp,
                                    @Nullable @Field("title") String title,
                                    @Nullable @Field("desc") String description,
                                    @Nullable @Field("conseqs") String consequences,
                                    @Nullable @Field("causes") String causes,
                                    @Nullable @Field("actions") String actions) {}
