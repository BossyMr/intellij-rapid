package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum UseTSPMode {
    @Deserializable("normal") NORMAL,
    @Deserializable("alltsk") ALL_TASK
}
