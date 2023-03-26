package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ServiceModel;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Path;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service("/rw/rapid/tasks")
public interface TaskService extends ServiceModel {

    @GET
    @NotNull NetworkCall<List<Task>> getTasks();

    @GET("/{task}")
    @NotNull NetworkCall<Task> getTask(
            @NotNull @Path("task") String task
    );

    @Subscribable("/rw/rapid/tasks;buildlogchange")
    @NotNull SubscribableNetworkCall<BuildLogEvent> onBuild();

}
