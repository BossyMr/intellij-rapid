package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
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

    @GET("{@self}/pcp")
    @NotNull NetworkCall<List<ProgramPointer>> getProgramPointer();

    @POST(value = "{@self}/pcp", arguments = "action=set-pp-cursor")
    @NotNull NetworkCall<Void> setProgramPointer(
            @NotNull @Field("module") String module,
            @NotNull @Field("routine") String routine,
            @Field("line") int row,
            @Field("column") int column
    );
}
