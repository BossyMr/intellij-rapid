package com.bossymr.network;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface NetworkQuery<T> {

    @Nullable T get() throws IOException, InterruptedException;

}
