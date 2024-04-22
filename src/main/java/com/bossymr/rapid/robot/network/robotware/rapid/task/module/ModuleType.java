package com.bossymr.rapid.robot.network.robotware.rapid.task.module;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum ModuleType {

    @Deserializable("ProgMod")
    PROGRAM_MODULE,

    @Deserializable("SysMod")
    SYSTEM_MODULE

}
