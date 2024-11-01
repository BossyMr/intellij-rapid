package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum RequestManualModePrivilege {

    @Alias("modify")
    MODIFY,

    @Alias("exec")
    EXECUTE,

    @Alias("deny")
    DENY
}
