package com.bossymr.rapid.network.controller;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("ctrl-identity-info")
public record IdentityEntity(@NotNull @Field("ctrl-name") String name,
                             @Nullable @Field("ctrl-id") String identifier) {}