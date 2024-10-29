package com.bossymr.rapid.robot.function;

import java.io.IOException;

@FunctionalInterface
public interface ThrowableConsumer<T> {
    void accept(T t) throws IOException, InterruptedException;
}
