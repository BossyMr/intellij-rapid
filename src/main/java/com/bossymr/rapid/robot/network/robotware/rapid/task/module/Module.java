package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module"})
public interface Module extends EntityModel {

    @Property("modname")
    @NotNull String getName();

    @Property("filename")
    @NotNull String getFileName();

    @POST(value = "{@self}", arguments = "action=save")
    @NotNull NetworkCall<Void> save(
            @NotNull @Field("name") String name,
            @NotNull @Field("path") String path
    );

    @GET(value = "{@self}")
    @NotNull NetworkCall<ModuleText> getText(
            @Argument("startrow") int startRow,
            @Argument("startcol") int startColumn,
            @Argument("endrow") int endRow,
            @Argument("endcol") int endColumn
    );

    @POST(value = "{@self}", arguments = "action=set-text-range")
    @NotNull NetworkCall<Void> setText(
            @NotNull @Field("task") String task,
            @NotNull @Field("replace-mode") ReplaceMode replaceMode,
            @NotNull @Field("query-mode") QueryMode queryMode,
            @Field("startrow") int startRow,
            @Field("startcol") int startColumn,
            @Field("endrow") int endRow,
            @Field("endcol") int endColumn,
            @NotNull @Field("text") String text
    );

    @POST(value = "{@self}", arguments = "action=set-module-text")
    @NotNull NetworkCall<UpdateModuleText> setText(
            @NotNull @Field("text") String text
    );

}
