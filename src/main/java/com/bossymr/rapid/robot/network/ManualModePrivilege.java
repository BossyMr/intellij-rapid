package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ManualModePrivilege {

    @Deserializable("none")
    NONE,

    @Deserializable("pending modify")
    PENDING_MODIFY,

    @Deserializable("modify")
    MODIFY,

    @Deserializable("exec")
    EXECUTE,
}
