package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity("rap-set-module-text")
public interface UpdateModuleText extends EntityModel {

    @Property("module-changed-name")
    boolean isNameChanged();

    @Property("new-modnam")
    @Nullable String getNewName();

    @Property("change-count")
    int getChangeCount();
}
