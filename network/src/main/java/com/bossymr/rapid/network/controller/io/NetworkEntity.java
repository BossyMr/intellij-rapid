package com.bossymr.rapid.network.controller.io;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@Entity("ios-network(-li)?")
public record NetworkEntity(@NotNull @Field("name") String name,
                            @NotNull @Field("pstate") PhysicalState physicalState,
                            @NotNull @Field("lstate") LogicalState logicalState) {

    public enum LogicalState {
        @JsonProperty("started")
        STARTED,
        @JsonProperty("stopped")
        STOPPED,
        @JsonProperty("unknown")
        UNKNOWN
    }

}
