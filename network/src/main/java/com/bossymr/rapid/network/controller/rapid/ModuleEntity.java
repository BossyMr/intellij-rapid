package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import com.bossymr.rapid.network.client.annotations.Title;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@Entity("rap-module-info-li")
public record ModuleEntity(@Title String title,
                           @NotNull @Field("name") String name,
                           @NotNull @Field("type") Type type) {

    enum Type {
        @JsonProperty("ProgMod")
        PROGRAM_MODULE,
        @JsonProperty("SysMod")
        SYSTEM_MODULE
    }

}
