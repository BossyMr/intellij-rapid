package com.bossymr.rapid.robot.network.query;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("progress-ev")
public interface AsynchronousEvent extends EntityModel {

    @Property("state")
    @NotNull String getState();

    @Property("code")
    @NotNull String getCode();

}
