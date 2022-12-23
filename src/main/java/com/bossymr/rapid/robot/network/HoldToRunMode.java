package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum HoldToRunMode {
    @Deserializable("press") PRESS,
    @Deserializable("held") HELD,
    @Deserializable("release") RELEASE,
}