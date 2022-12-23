package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-rap-buildlog-ev")
public interface BuildLogEvent extends EntityModel {

    @Property("task-name")
    @NotNull String getTask();

    @Property("build-count")
    int getBuildCount();

    @Property("build-log-change")
    @NotNull BuildLogState getState();

}
