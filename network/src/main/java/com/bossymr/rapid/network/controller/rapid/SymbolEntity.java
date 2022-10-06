package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;

@Entity("rap-symprop([a-z]+)(-li)?")
public record SymbolEntity(@NotNull @Field("symburl") String symbol,
                           @NotNull @Field("name") String name) {}
