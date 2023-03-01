package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-mod-text")
public interface ModuleText extends EntityModel {

    @Property("text")
    @NotNull String getText();

}
