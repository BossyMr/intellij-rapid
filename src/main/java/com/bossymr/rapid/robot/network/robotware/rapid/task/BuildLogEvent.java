package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-buildlog-ev")
public interface BuildLogEvent extends EntityModel {

    @Property("task-name")
    @NotNull String getTask();

    @Property("build-count")
    int getBuildCount();

    @Property("build-log-change")
    @NotNull BuildLogState getState();

}
