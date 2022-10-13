package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@Entity("rap-task(-li)?")
public record TaskEntity(@NotNull @Field("name") String name,
                         @NotNull @Field("taskstate") State state,
                         @NotNull @Field("excstate") ExecutionState executionState) {

    enum State {
        @JsonProperty("empty")
        EMPTY,
        @JsonProperty("initiated")
        INITIATED,
        @JsonProperty("linked")
        LINKED,
        @JsonProperty("loaded")
        LOADED
    }

    enum ExecutionState {
        @JsonProperty("ready")
        READY,
        @JsonProperty("stopped")
        STOPPED,
        @JsonProperty("started")
        STARTED,
        @JsonProperty("uninitialized")
        UNINITIALIZED
    }

}
