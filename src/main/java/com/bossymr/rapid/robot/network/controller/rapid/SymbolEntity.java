package com.bossymr.rapid.robot.network.controller.rapid;

import com.bossymr.rapid.robot.network.client.annotations.Entity;
import com.bossymr.rapid.robot.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("rap-symprop([a-z]+)(-li)?")
public record SymbolEntity(@NotNull @Field("symburl") String symbol,
                           @NotNull @Field("name") String name,
                           @NotNull @Field("symtyp") SymbolType symbolType,
                           @Nullable @Field("dattyp") String dataType,
                           @Nullable @Field("typurl") String canonicalDataType,
                           @Nullable @Field("mode") String mode,
                           @Nullable @Field("ndim") Integer dimensions,
                           @Nullable @Field("dim") Integer length,
                           @Nullable @Field("required") Boolean required,
                           @Nullable @Field("ncom") Integer componentLength,
                           @Nullable @Field("npar") Integer parameterLength,
                           @Nullable @Field("comnum") Integer componentIndex,
                           @Nullable @Field("parnum") Integer parameterIndex) {}
