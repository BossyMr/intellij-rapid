package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import com.bossymr.rapid.network.client.annotations.Title;
import org.jetbrains.annotations.Nullable;

@Entity("elog-domain(-li)?")
public record EventLogCategoryEntity(@Title String title,
                                     @Nullable @Field("domain-name") String name,
                                     @Field("numevts") int size,
                                     @Field("buffsize") int capacity) {}
