package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module"})
public interface Module extends EntityModel {

    @Property("modname")
    @NotNull String getName();

    @Property("filename")
    @NotNull String getFileName();

    @POST("{@self}&action=save")
    @NotNull NetworkCall<Void> save(
            @NotNull @Field("name") String name,
            @NotNull @Field("path") String path
    );

    @POST("{@self}&action=set-module-text")
    @NotNull NetworkCall<UpdateModuleText> setText(
            @NotNull @Field("text") String text
    );

}
