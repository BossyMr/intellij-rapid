package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

public enum StopMode {
    @Deserializable("cycle") CYCLE,
    @Deserializable("instr") INSTRUCTION,
    @Deserializable("stop") STOP,
    @Deserializable("qstop") QUICK_STOP
}
