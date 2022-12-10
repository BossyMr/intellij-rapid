package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum UserLocale {

    @Deserializable("remote")
    REMOTE,

    @Deserializable("local")
    LOCAL
}
