package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service("/rw/rapid/tasks")
public interface TaskService {

    @GET("")
    @NotNull NetworkCall<List<Task>> getTasks();

    @Subscribable("/rw/rapid/tasks;buildlogchange")
    @NotNull SubscribableNetworkCall<BuildLogEvent> onBuild();

}
