package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum QueryMode {
    @Deserializable("Force") FORCE,
    @Deserializable("Try") TRY
}
