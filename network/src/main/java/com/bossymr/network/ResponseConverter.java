package com.bossymr.network;

import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface ResponseConverter<T> {

    /**
     * Converts the specified response into the specified type.
     *
     * @param response the response.
     * @return the result.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    @Nullable T convert(@NotNull Response response) throws IOException, InterruptedException;

}
