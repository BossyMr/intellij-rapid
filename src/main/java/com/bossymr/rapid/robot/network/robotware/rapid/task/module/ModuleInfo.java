package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module-info-li", "rap-module-info"})
public interface ModuleInfo extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("type")
    @NotNull ModuleType getModuleType();

    @GET("/rw/rapid/modules/{module}")
    @NotNull NetworkCall<Module> getModule(
            @NotNull @Path("module") String module,
            @NotNull @Argument("task") String task
    );

    default @NotNull NetworkCall<Module> getModule() {
        String title = getTitle();
        String task = title.substring(0, title.lastIndexOf('/'));
        String module = title.substring(title.lastIndexOf('/') + 1);
        return getModule(module, task);
    }

}
