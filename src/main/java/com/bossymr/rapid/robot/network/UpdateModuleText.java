package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
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