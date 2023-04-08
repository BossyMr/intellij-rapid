package com.bossymr.network;

import com.bossymr.network.client.GenericType;
import com.bossymr.network.client.NetworkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResponseConverterFactory {

    <T> @Nullable ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull GenericType<T> type);

}
