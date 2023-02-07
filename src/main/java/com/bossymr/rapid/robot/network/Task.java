package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"rap-task", "rap-task-li"})
public interface Task extends EntityModel {

    @GET("{@modules}")
    @NotNull NetworkCall<List<ModuleInfo>> getModules();

    @GET("{@program}")
    @NotNull NetworkCall<Program> getProgram();

    @Property("name")
    @NotNull String getName();

}
