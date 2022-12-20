package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Service("/rw/rapid/tasks")
public interface TaskService {

    @GET("")
    @NotNull Query<List<Task>> getTasks();

}
