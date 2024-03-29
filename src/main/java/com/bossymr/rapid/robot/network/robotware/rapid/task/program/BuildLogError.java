package com.bossymr.rapid.robot.network.robotware.rapid.task.program;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-builderrs")
public interface BuildLogError {

    @Property("ModuleName")
    @NotNull String getModuleName();

    @Property("row")
    int getRow();

    @Property("column")
    int getColumn();

    @Property("error")
    @NotNull String getErrorMessage();

}
