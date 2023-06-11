package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import com.bossymr.network.client.FetchMethod;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"rap-task", "rap-task-li"})
public interface Task {

    @Fetch("{@modules}")
    @NotNull NetworkQuery<List<ModuleInfo>> getModules();

    @Fetch("{@modules}/{module}")
    @NotNull NetworkQuery<ModuleInfo> getModule(@NotNull @Path("module") String module);

    @Fetch("{@program}")
    @NotNull NetworkQuery<Program> getProgram();

    @Property("name")
    @NotNull String getName();

    @Property("motiontask")
    boolean isMotionTask();

    @Property("tasktype")
    @NotNull TaskType getTaskType();

    @Property("excstate")
    @NotNull TaskExecutionState getExecutionState();

    @Property("active")
    @NotNull TaskActiveState getActivityState();

    @Fetch("{@self}/pcp")
    @NotNull NetworkQuery<List<ProgramPointer>> getProgramPointer();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=activate")
    @NotNull NetworkQuery<Void> activate();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=deactivate")
    @NotNull NetworkQuery<Void> deactivate();

    @Fetch(method = FetchMethod.POST, value = "{@self}/pcp", arguments = "action=set-pp-cursor")
    @NotNull NetworkQuery<Void> setProgramPointer(@NotNull @Field("module") String module,
                                                  @NotNull @Field("routine") String routine,
                                                  @Field("line") int row,
                                                  @Field("column") int column);

    @Fetch(value = "{@self}", arguments = "resource=activation-record")
    @NotNull NetworkQuery<StackFrame> getStackFrame(@Argument("stackframe") int stackframe);

    @Subscribable("{@self};excstate")
    @NotNull SubscribableNetworkQuery<TaskExecutionEvent> onExecutionState();

    @Subscribable("{@self}/pcp;programpointerchange")
    @NotNull SubscribableNetworkQuery<ProgramPointerEvent> onProgramPointer();

}