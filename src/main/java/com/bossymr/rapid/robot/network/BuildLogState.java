package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum BuildLogState {

    @Deserializable("SYS_CTRL_S_RAPID_SEMANTIC_ERROR") SEMANTIC_ERROR,
    @Deserializable("SYS_CTRL_S_RAPID_SYNTAX_ERROR") SYNTAX_ERROR,
    @Deserializable("SYS_CTRL_S_OK") OK

}
