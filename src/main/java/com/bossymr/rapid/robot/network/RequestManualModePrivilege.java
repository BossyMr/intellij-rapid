package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum RequestManualModePrivilege {

    @Deserializable("modify")
    MODIFY,

    @Deserializable("exec")
    EXECUTE,

    @Deserializable("deny")
    DENY
}
