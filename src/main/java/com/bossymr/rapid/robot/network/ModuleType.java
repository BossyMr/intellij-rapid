package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;

public enum ModuleType {

    @Deserializable("ProgMod") PROGRAM_MODULE,

    @Deserializable("SysMod") SYSTEM_MODULE

}
