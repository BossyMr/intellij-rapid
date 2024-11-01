package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum InputOutputNetworkConfigurationType {
    @Alias("BITS") BITS,
    @Alias("GROUPS") GROUPS,
    @Alias("BOTH") BOTH,
    @Alias("SCAN") SCAN,
    @Alias("UNITS") UNITS,
}
