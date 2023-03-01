package com.bossymr.rapid.robot.network.robotware.rapid.task;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-execstate-ev")
public interface TaskExecutionEvent extends EntityModel {

    @Property("pgmtaskexec-state")
    @NotNull TaskExecutionState getState();

}
