package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum ExecutionMode {
    @Deserializable("continue") CONTINUE,
    @Deserializable("stepin") STEP_IN,
    @Deserializable("stepover") STEP_OVER,
    @Deserializable("stepout") STEP_OUT,
    @Deserializable("stepback") STEP_BACK,
    @Deserializable("steplast") STEP_LAST,
    @Deserializable("stepmotion") STEP_MOTION
}
