package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ConditionState {
    @Deserializable("none") NONE,
    @Deserializable("callchain") CALLCHAIN
}
