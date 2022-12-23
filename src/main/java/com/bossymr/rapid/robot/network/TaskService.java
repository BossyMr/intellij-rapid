package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service("/rw/rapid/tasks")
public interface TaskService {

    @GET("")
    @NotNull Query<List<Task>> getTasks();

    @Subscribable("/rw/rapid/tasks;buildlogchange")
    @NotNull SubscribableQuery<BuildLogEvent> onBuild();

}
