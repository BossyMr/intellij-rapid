package com.bossymr.network;

import com.bossymr.network.client.GenericType;
import com.bossymr.network.client.NetworkAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResponseConverterFactory {

    <T> @Nullable ResponseConverter<T> create(@NotNull NetworkAction action, @NotNull GenericType<T> type);

}
