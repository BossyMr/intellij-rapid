package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;

@Entity({"rap-module-info-li", "rap-module-info"})
public interface ModuleInfo extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("type")
    @NotNull ModuleType getModuleType();

    default @NotNull Query<Module> getModule() {
        // It is not possible to invoke this method through annotations, as a link is not provided to it.
        // An alternative would be to invoke another method with the correct name and task, but that method would need
        // to be public.
        String task = getTitle().substring(0, getTitle().lastIndexOf('/'));
        String name = getTitle().substring(getTitle().lastIndexOf('/') + 1);
        HttpRequest request = HttpRequest.newBuilder(getNetworkClient().getDefaultPath().resolve("/rw/rapid/modules/" + name + "?task=" + task)).build();
        return getNetworkClient().newQuery(request, Module.class);
    }

}
