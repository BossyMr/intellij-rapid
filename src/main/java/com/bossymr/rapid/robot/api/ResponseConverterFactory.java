package com.bossymr.rapid.robot.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResponseConverterFactory {

    /**
     * Create a converter to convert a response into the specified type.
     *
     * @param manager the {@code NetworkManager}.
     * @param type the response type.
     * @param <T> the response type.
     * @return the response converter, or {@code null} if this factory cannot convert to the specified type.
     */
    <T> @Nullable ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull GenericType<T> type);

}
