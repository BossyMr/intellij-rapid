package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Path;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.annotations.Subscribable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service("/rw/rapid/tasks")
public interface TaskService {

    @Fetch("")
    @NotNull NetworkQuery<List<Task>> getTasks();

    @Fetch("/{task}")
    @NotNull NetworkQuery<Task> getTask(@NotNull @Path("task") String task);

    @Subscribable("/rw/rapid/tasks;buildlogchange")
    @NotNull SubscribableNetworkQuery<BuildLogEvent> onBuild();

}
