package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ExecutionCycle {

    @Deserializable("forever")
    FOREVER,

    @Deserializable("asis")
    AS_IS,

    @Deserializable("once")
    ONCE,

    @Deserializable("oncedone")
    ONCE_DONE

}
