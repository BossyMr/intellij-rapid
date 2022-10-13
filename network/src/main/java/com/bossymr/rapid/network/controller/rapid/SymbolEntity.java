package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("rap-symprop([a-z]+)(-li)?")
public record SymbolEntity(@NotNull @Field("symburl") String symbol,
                           @NotNull @Field("name") String name,
                           @NotNull @Field("symtyp") SymbolType symbolType,
                           @NotNull @Field("dattyp") String dataType,
                           @NotNull @Field("typurl") String canonicalDataType,
                           @Field("ndim") int dimensions,
                           @Nullable @Field("dim") Integer length) {}
