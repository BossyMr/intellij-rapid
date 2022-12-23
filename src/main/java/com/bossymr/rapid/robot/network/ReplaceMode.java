package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ReplaceMode {
    @Deserializable("After") AFTER,
    @Deserializable("Before") BEFORE,
    @Deserializable("Replace") REPLACE
}
