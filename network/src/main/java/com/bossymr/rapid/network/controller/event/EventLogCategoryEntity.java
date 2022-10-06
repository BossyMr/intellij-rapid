package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import org.jetbrains.annotations.Nullable;

@Entity("elog-domain(-li)?")
public record EventLogCategoryEntity(@Nullable @Field("domain-name") String name,
                                     @Field("numvets") int size,
                                     @Field("buffsize") int capacity) {}
