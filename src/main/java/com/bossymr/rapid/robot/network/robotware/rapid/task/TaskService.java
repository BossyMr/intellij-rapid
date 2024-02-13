package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Path;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
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
