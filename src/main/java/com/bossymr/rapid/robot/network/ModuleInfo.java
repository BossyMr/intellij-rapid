package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-module-info-li", "rap-module-info"})
public interface ModuleInfo extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("type")
    @NotNull ModuleType getModuleType();

    /**
     * This method should not be called directly. As a response model does not contain a link to the corresponding
     * model, the name of the module and the task need to be calculated using the response entity.
     *
     * @see #getModule()
     */
    @GET("/rw/rapid/modules/{name}")
    @NotNull Query<Module> getInternalModule(
            @NotNull @Path("name") String name,
            @NotNull @Argument("task") String task
    );

    default @NotNull Query<Module> getModule() {
        String task = getTitle().substring(0, getTitle().lastIndexOf('/'));
        String name = getTitle().substring(getTitle().lastIndexOf('/') + 1);
        return getInternalModule(name, task);
    }

}
