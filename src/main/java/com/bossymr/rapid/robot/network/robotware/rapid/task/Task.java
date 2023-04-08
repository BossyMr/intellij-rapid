package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"rap-task", "rap-task-li"})
public interface Task {

    @Fetch("{@modules}")
    @NotNull List<ModuleInfo> getModules();

    @Fetch("{@program}")
    @NotNull Program getProgram();

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
    @NotNull List<ProgramPointer> getProgramPointer();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=activate")
    @NotNull Void activate();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=deactivate")
    @NotNull Void deactivate();

    @Fetch(method = FetchMethod.POST, value = "{@self}/pcp", arguments = "action=set-pp-cursor")
    @NotNull Void setProgramPointer(
            @NotNull @Field("module") String module,
            @NotNull @Field("routine") String routine,
            @Field("line") int row,
            @Field("column") int column
    );

    @Fetch(value = "{@self}", arguments = "resource=activation-record")
    @NotNull StackFrame getStackFrame(
            @Argument("stackframe") int stackframe
    );

    @Subscribable("{@self};excstate")
    @NotNull SubscribableNetworkQuery<TaskExecutionEvent> onExecutionState();

    @Subscribable("{@self}/pcp;programpointerchange")
    @NotNull SubscribableNetworkQuery<ProgramPointerEvent> onProgramPointer();

}
