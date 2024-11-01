package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Alias;

public enum ManualModePrivilege {

    @Alias("none")
    NONE,

    @Alias("pending modify")
    PENDING_MODIFY,

    @Alias("modify")
    MODIFY,

    @Alias("exec")
    EXECUTE,
}
