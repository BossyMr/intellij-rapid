package com.bossymr.rapid.robot.api;

import java.io.IOException;

public interface NetworkQuery<T> {

    T get() throws IOException, InterruptedException;

}
