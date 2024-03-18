package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum UserLocale {

    @Deserializable("remote")
    REMOTE,

    @Deserializable("local")
    LOCAL
}
