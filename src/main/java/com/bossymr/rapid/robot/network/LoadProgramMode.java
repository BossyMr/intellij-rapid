package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum LoadProgramMode {
    @Deserializable("add") ADD,
    @Deserializable("replace") REPLACE,
}
