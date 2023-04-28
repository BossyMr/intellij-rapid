package com.bossymr.network;

import java.io.IOException;

public interface NetworkQuery<T> {

    T get() throws IOException, InterruptedException;

}
