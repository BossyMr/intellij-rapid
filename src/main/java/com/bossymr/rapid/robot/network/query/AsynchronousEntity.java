package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

public interface AsynchronousEntity extends SubscribableQuery<AsynchronousEvent> {

    @NotNull Query<AsynchronousEvent> poll();

}
