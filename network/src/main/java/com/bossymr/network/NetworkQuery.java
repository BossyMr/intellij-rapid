package com.bossymr.network;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface NetworkQuery<T> {

    T get() throws IOException, InterruptedException;

}
