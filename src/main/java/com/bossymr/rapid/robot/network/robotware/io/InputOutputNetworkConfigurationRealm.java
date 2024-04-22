package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum InputOutputNetworkConfigurationRealm {
    @Deserializable("1") RUNTIME,
    @Deserializable("2") GENERAL,
    @Deserializable("3") BOTH
}
