package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

public interface AsynchronousQuery {

    @NotNull AsynchronousEntity send();

}
