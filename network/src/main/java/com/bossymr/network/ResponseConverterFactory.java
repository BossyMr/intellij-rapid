package com.bossymr.network;

import com.bossymr.network.client.GenericType;
import com.bossymr.network.client.NetworkManager;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpResponse;

public interface ResponseConverterFactory {

    <T> ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull HttpResponse<?> response, @NotNull GenericType<T> type);

}
