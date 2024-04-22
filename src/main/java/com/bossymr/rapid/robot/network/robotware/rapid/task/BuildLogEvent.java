package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-buildlog-ev")
public interface BuildLogEvent {

    @Property("task-name")
    @NotNull String getTask();

    @Property("build-count")
    int getBuildCount();

    @Property("build-log-change")
    @NotNull BuildLogState getState();

}
