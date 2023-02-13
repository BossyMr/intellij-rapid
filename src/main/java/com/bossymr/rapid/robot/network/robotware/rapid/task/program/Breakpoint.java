package com.bossymr.rapid.robot.network.robotware.rapid.task.program;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-program-breakpoint")
public interface Breakpoint extends EntityModel {

    @Property("module-name")
    @NotNull String getModule();

    @Property("start-row")
    int getStartRow();

    @Property("end-row")
    int getEndRow();

    @Property("start-col")
    int getStartColumn();

    @Property("end-col")
    int getEndColumn();

}
