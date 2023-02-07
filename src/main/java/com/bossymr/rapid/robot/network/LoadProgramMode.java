package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;

public enum LoadProgramMode {
    @Deserializable("add") ADD,
    @Deserializable("replace") REPLACE,
}
