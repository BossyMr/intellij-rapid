package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module"})
public interface ModuleEntity {

    @Property("modname")
    @NotNull String getName();

    @Property("filename")
    @NotNull String getFileName();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=save")
    @NotNull NetworkQuery<Void> save(@NotNull @Field("name") String name,
                                     @NotNull @Field("path") String path);

    @Fetch(value = "{@self}")
    @NotNull NetworkQuery<ModuleText> getText(@Argument("startrow") int startRow,
                                              @Argument("startcol") int startColumn,
                                              @Argument("endrow") int endRow,
                                              @Argument("endcol") int endColumn);

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=set-text-range")
    @NotNull NetworkQuery<Void> setText(@NotNull @Field("task") String task,
                                        @NotNull @Field("replace-mode") ReplaceMode replaceMode,
                                        @NotNull @Field("query-mode") QueryMode queryMode,
                                        @Field("startrow") int startRow,
                                        @Field("startcol") int startColumn,
                                        @Field("endrow") int endRow,
                                        @Field("endcol") int endColumn,
                                        @NotNull @Field("text") String text);

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=set-module-text")
    @NotNull NetworkQuery<UpdateModuleText> setText(@NotNull @Field("text") String text);

}
