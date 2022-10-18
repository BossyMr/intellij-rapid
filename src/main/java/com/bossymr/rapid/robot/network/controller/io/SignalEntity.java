package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.client.annotations.Entity;
import com.bossymr.rapid.robot.network.client.annotations.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@Entity("ios-signal(-li)?")
public record SignalEntity(@NotNull @Field("name") String name,
                           @NotNull @Field("type") SignalType signalType,
                           @NotNull @Field("category") String category,
                           @Field("lvalue") int logicalValue,
                           @NotNull @Field("lstate") SignalState logicalState,
                           @NotNull @Field("unitnm") String device,
                           @NotNull @Field("phstate") PhysicalState physicalState,
                           @NotNull @Field("pvalue") String physicalValue) {


    enum PhysicalState {
        @JsonProperty("invalid")
        INVALID,
        @JsonProperty("valid")
        VALID
    }

    enum SignalState {
        @JsonProperty("simulated")
        SIMULATED,
        @JsonProperty("not simulated")
        NOT_SIMULATED
    }

}
