package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity("rap-set-module-text")
public interface UpdateModuleText {

    @Property("module-changed-name")
    boolean isNameChanged();

    @Property("new-modnam")
    @Nullable String getNewName();

    @Property("change-count")
    int getChangeCount();
}
