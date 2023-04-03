package com.bossymr.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface ResponseConverter<T> {

    @Nullable T convert(@NotNull HttpResponse<byte[]> response) throws IOException, InterruptedException;

}
