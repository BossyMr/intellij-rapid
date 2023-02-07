package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Deserializable;

public enum RegainState {

    @Deserializable("continue") CONTINUE,
    @Deserializable("regain") REGAIN,
    @Deserializable("clear") CLEAR

}
