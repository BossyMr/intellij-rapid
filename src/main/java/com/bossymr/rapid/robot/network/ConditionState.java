package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;

public enum ConditionState {
    @Deserializable("none") NONE,
    @Deserializable("callchain") CALLCHAIN
}
