package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.NetworkQuery;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module-info-li", "rap-module-info"})
public interface ModuleInfo {

    @Title
    @NotNull String getTitle();

    @Property("name")
    @NotNull String getName();

    @Property("type")
    @NotNull ModuleType getModuleType();

        @Fetch("/rw/rapid/modules/{module}")
  @NotNull NetworkQuery<ModuleEntity> getModule(@NotNull @Path("module") String module,
                                         @NotNull @Argument("task") String task);

    default @NotNull NetworkQuery<ModuleEntity> getModule() {
        String title = getTitle();
        String task = title.substring(0, title.lastIndexOf('/'));
        String module = title.substring(title.lastIndexOf('/') + 1);
        return getModule(module, task);
    }

}
