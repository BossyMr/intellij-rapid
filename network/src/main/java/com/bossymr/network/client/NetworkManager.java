package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.ResponseConverter;
import com.bossymr.network.ResponseConverterFactory;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.client.proxy.ListProxy;
import com.bossymr.network.client.response.EntityConverter;
import com.bossymr.network.client.response.ResponseModelConverter;
import com.bossymr.network.client.response.StringConverter;
import com.bossymr.network.client.security.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

public class NetworkManager {

    private final @NotNull NetworkClient networkClient;

    private final @NotNull Set<ResponseConverterFactory> converters;

    private volatile boolean closed;

    public NetworkManager(@NotNull URI defaultPath, @Nullable Credentials credentials) {
        this.networkClient = new NetworkClient(defaultPath, credentials);
        this.converters = Set.of(StringConverter.FACTORY, ResponseModelConverter.FACTORY, EntityConverter.FACTORY);
    }

    public @NotNull NetworkAction createAction() {
        return new NetworkAction(this);
    }

    /**
     * Returns the {@code NetworkClient} of this {@code NetworkManager}. All entities sent by entities managed by this
     * {@code EntityManager} will be sent to this {@code NetworkClient}.
     *
     * @return the {@code NetworkClient}.
     */
    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    <T> @NotNull NetworkQuery<T> createQuery(@NotNull NetworkAction action, @NotNull Class<T> type, @NotNull HttpRequest request) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return createQuery(action, GenericType.of(type), request);
    }

    @SuppressWarnings("unchecked")
    <T> @NotNull NetworkQuery<T> createQuery(@NotNull NetworkAction action, @NotNull GenericType<T> type, @NotNull HttpRequest request) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return () -> {
            if (type.getRawType().equals(List.class)) {
                ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                Class<T> typeArgument = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                return (T) new ListProxy<>(action, typeArgument, request);
            }
            HttpResponse<byte[]> response = networkClient.send(request);
            for (ResponseConverterFactory converter : converters) {
                ResponseConverter<T> responseConverter = converter.create(action, type);
                if (responseConverter != null) {
                    return responseConverter.convert(response);
                }
            }
            return null;
        };
    }

    <T> @NotNull SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull NetworkAction action, @NotNull SubscribableEvent<T> subscribableEvent) {
        return (priority, listener) -> networkClient.subscribe(subscribableEvent, priority, (entity, event) -> {
            T response = action.createEntity(subscribableEvent.getEventType(), event);
            if (response != null) {
                listener.onEvent(entity, response);
            }
        });
    }

    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        closed = true;
        networkClient.close();
    }
}
