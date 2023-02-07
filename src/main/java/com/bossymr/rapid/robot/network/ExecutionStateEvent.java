package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-ctrlexecstate-ev")
public interface ExecutionStateEvent extends EntityModel {

    @Property("ctrlexecstate")
    @NotNull ExecutionState getState();

}
