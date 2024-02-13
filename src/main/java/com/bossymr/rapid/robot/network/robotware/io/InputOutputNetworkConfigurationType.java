package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.annotations.Deserializable;

public enum InputOutputNetworkConfigurationType {
    @Deserializable("BITS") BITS,
    @Deserializable("GROUPS") GROUPS,
    @Deserializable("BOTH") BOTH,
    @Deserializable("SCAN") SCAN,
    @Deserializable("UNITS") UNITS,
}
