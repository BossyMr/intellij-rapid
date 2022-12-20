package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Field;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module"})
public interface Module extends EntityModel {

    @Property("modname")
    @NotNull String getName();

    @Property("filename")
    @NotNull String getFileName();

    @POST("{@self}&action=save")
    @NotNull Query<Void> save(
            @NotNull @Field("name") String name,
            @NotNull @Field("path") String path
    );

    @POST("{@self}&action=set-module-text")
    @NotNull Query<UpdateModuleText> setText(
            @NotNull @Field("text") String text
    );

}
