package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"rap-task", "rap-task-li"})
public interface Task {

    @Fetch("{@modules}")
    @NotNull NetworkQuery<List<ModuleInfo>> getModules();

    @Fetch("{@modules}/{module}")
    @NotNull NetworkQuery<ModuleEntity> getModule(@NotNull @Path("module") String module);

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

    @Fetch("/rw/rapid/tasks/{#name}/pcp")
    @NotNull NetworkQuery<List<ProgramPointer>> getProgramPointer();

    @Fetch(method = FetchMethod.POST, value = "/rw/rapid/tasks/{#name}", arguments = "action=activate")
    @NotNull NetworkQuery<Void> activate();

    @Fetch(method = FetchMethod.POST, value = "/rw/rapid/tasks/{#name}", arguments = "action=deactivate")
    @NotNull NetworkQuery<Void> deactivate();

    @Fetch(method = FetchMethod.POST, value = "/rw/rapid/tasks/{#name}", arguments = "action=unloadmod")
    @NotNull NetworkQuery<Void> loadModule(@NotNull @Field("modulepath") String modulePath,
                                           @Field("replace") boolean replace);

    @Fetch(method = FetchMethod.POST, value = "/rw/rapid/tasks/{#name}", arguments = "action=unloadmod")
    @NotNull NetworkQuery<Void> unloadModule(@NotNull @Field("module") String moduleName);

    @Fetch(method = FetchMethod.POST, value = "/rw/rapid/tasks/{#name}/pcp", arguments = "action=set-pp-cursor")
    @NotNull NetworkQuery<Void> setProgramPointer(@NotNull @Field("module") String module,
                                                  @NotNull @Field("routine") String routine,
                                                  @Field("line") int row,
                                                  @Field("column") int column);

    @Fetch(value = "/rw/rapid/tasks/{#name}", arguments = "resource=activation-record")
    @NotNull NetworkQuery<StackFrame> getStackFrame(@Argument("stackframe") int stackframe);

    @Subscribable("/rw/rapid/tasks/{#name};excstate")
    @NotNull SubscribableNetworkQuery<TaskExecutionEvent> onExecutionState();

    @Subscribable("/rw/rapid/tasks/{#name}/pcp;programpointerchange")
    @NotNull SubscribableNetworkQuery<ProgramPointerEvent> onProgramPointer();

}
