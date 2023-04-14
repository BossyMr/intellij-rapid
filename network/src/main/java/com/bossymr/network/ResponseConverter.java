package com.bossymr.network;

import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface ResponseConverter<T> {

    @Nullable T convert(@NotNull Response response) throws IOException, InterruptedException;

}
