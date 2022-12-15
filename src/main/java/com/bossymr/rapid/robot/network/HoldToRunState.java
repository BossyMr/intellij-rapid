package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum HoldToRunState {
    @Deserializable("HdTREvent WaitEntered") WAIT_ENTERED,
    @Deserializable("HdTREvent WaitLeft") WAIT_LEFT
}
