package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.client.annotations.Entity;
import com.bossymr.rapid.robot.network.client.annotations.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@Entity("ios-device(-li)?")
public record DeviceEntity(@NotNull @Field("name") String name,
                           @NotNull @Field("pstate") PhysicalState physicalState,
                           @NotNull @Field("lstate") LogicalState logicalState,
                           @NotNull @Field("address") String address) {


    enum LogicalState {
        @JsonProperty("enabled")
        ENABLED,
        @JsonProperty("disabled")
        DISABLED
    }

}
