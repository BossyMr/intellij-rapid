package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"rap-task", "rap-task-li"})
public interface Task extends EntityModel {

    @GET("{@modules}")
    @NotNull Query<List<ModuleInfo>> getModules();

    @GET("{@program}")
    @NotNull Query<Program> getProgram();

    @Property("name")
    @NotNull String getName();

}
