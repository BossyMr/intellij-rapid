package com.bossymr.network.client;

import com.bossymr.network.*;
import com.bossymr.network.client.response.EntityConverter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LightNetworkManager implements NetworkManager {

    private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();
    private final @NotNull NetworkManager manager;

    public LightNetworkManager(@NotNull NetworkManager manager) {
        this.manager = manager;
    }

    @Override
    public @NotNull NetworkClient getNetworkClient() {
        return manager.getNetworkClient();
    }

    @Override
    public @NotNull Set<ResponseConverterFactory> getConverters() {
        return manager.getConverters();
    }

    @Override
    public @NotNull NetworkManager createLight() {
        return new LightNetworkManager(this);
    }

    @Override
    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull GenericType<T> type, @NotNull NetworkRequest request) {
        return HeavyNetworkManager.createQuery(this, type, request);
    }

    @Override
    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> event) {
        return (priority, listener) -> {
            SubscriptionEntity entity = getNetworkClient().subscribe(event, priority, new SubscriptionListener<>() {
                @Override
                public void onEvent(@NotNull SubscriptionEntity entity, @NotNull EntityModel response) {
                    EntityConverter<T> converter = new EntityConverter<>(LightNetworkManager.this, GenericType.of(event.getEventType()));
                    T result = converter.convert(response);
                    if (result != null) {
                        listener.onEvent(entity, result);
                    }
                }

                @Override
                public void onClose(@NotNull SubscriptionEntity entity) {
                    listener.onClose(entity);
                    entities.remove(entity);
                }
            });
            entities.add(entity);
            return entity;
        };
    }

    @Override
    public <T> @NotNull T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        return HeavyNetworkManager.createEntity(manager, entityType, model);
    }

    @Override
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        return HeavyNetworkManager.createService(manager, serviceType);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        SubscriptionEntity.unsubscribe(entities);
    }

}
