package com.bossymr.rapid.robot.function;

import java.io.IOException;

@FunctionalInterface
public interface ThrowableFunction<T, R> {
    R apply(T t) throws IOException, InterruptedException;
}
