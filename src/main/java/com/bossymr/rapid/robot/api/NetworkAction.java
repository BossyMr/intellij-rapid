package com.bossymr.rapid.robot.api;

import com.bossymr.rapid.robot.api.client.HeavyNetworkManager;
import com.bossymr.rapid.robot.api.client.NetworkClient;
import com.bossymr.rapid.robot.api.client.SubscribableEvent;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.response.EntityConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAction implements NetworkManager {

    private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();
    private final @NotNull NetworkManager manager;
    private final @NotNull Set<Listener> listeners = ConcurrentHashMap.newKeySet();

    private volatile boolean closed;

    /**
     * Creates a new {@code NetworkAction} which will delegate all requests to the specified {@code NetworkManager}.
     * <p>
     * This {@code NetworkAction} will close independently of the specified manager, and will only close subscriptions
     * created by entities managed by this action. If the specified manager is closed, this action will also be closed.
     *
     * @param manager the manager.
     */
    public NetworkAction(@NotNull NetworkManager manager) {
        this.manager = manager;
        manager.subscribe(new Listener() {
            @Override
            public void onClose() throws IOException, InterruptedException {
                NetworkAction.this.close();
            }
        });
    }


    /**
     * This method is called for each successful response.
     *
     * @param request the request.
     * @param entity the entity.
     * @param <T> the entity type.
     * @return whether to elevate this response.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if the current thread is interrupted.
     */
    protected <T> boolean onSuccess(@NotNull NetworkQuery<T> request, @Nullable T entity) throws IOException, InterruptedException {
        return true;
    }

    /**
     * This method is called for each unsuccessful response.
     *
     * @param request the request.
     * @param throwable the exception.
     * @return whether to elevate this response.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if the current thread is interrupted.
     */
    protected boolean onFailure(@NotNull NetworkQuery<?> request, @NotNull Throwable throwable) throws IOException, InterruptedException {
        close();
        return true;
    }

    @Override
    public @NotNull NetworkClient getNetworkClient() {
        return manager.getNetworkClient();
    }

    @Override
    public <T> @NotNull T move(@NotNull T entity) {
        return HeavyNetworkManager.move(entity, this);
    }

    @Override
    public void subscribe(@NotNull NetworkManager.Listener listener) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        listeners.add(listener);
    }

    @Override
    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull NetworkQuery<HttpResponse<byte[]>> request, @NotNull GenericType<T> type) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        NetworkQuery<T> query = HeavyNetworkManager.createQuery(this, request, type);
        return new NetworkQuery<>() {
            @Override
            public GenericType<T> getType() {
                return query.getType();
            }

            @Override
            public URI getPath() {
                return query.getPath();
            }

            @Override
            public T get() throws IOException, InterruptedException {
                try {
                    T response = query.get();
                    NetworkManager entity = NetworkAction.this;
                    while (entity instanceof NetworkAction action) {
                        if (!(onSuccess(query, response))) {
                            break;
                        }
                        entity = action.manager;
                    }
                    return response;
                } catch (IOException | RuntimeException e) {
                    onException(request, e);
                    throw e;
                }
            }
        };
    }

    @Override
    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> event) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return (priority, listener) -> {
            try {
                SubscriptionEntity entity = getNetworkClient().subscribe(event, priority, new SubscriptionListener<>() {
                    @Override
                    public void onEvent(@NotNull SubscriptionEntity entity, @NotNull EntityModel response) {
                        EntityConverter<T> converter = new EntityConverter<>(NetworkAction.this, GenericType.of(event.getEventType()));
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
            } catch (IOException | RuntimeException e) {
                NetworkQuery<HttpResponse<byte[]>> request = getNetworkClient().newRequest(URI.create("/subscription")).build();
                onException(request, e);
                throw e;
            }
        };
    }

    private <T> void onException(@NotNull NetworkQuery<T> request, @NotNull Exception e) throws IOException, InterruptedException {
        NetworkManager entity = this;
        while (entity instanceof NetworkAction action) {
            if (!(onFailure(request, e))) {
                break;
            }
            entity = action.manager;
        }
    }

    @Override
    public <T> @NotNull T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return HeavyNetworkManager.createEntity(this, entityType, model);
    }

    @Override
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return HeavyNetworkManager.createService(this, serviceType);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        for (Listener listener : listeners) {
            listener.onClose();
        }
        closed = true;
        SubscriptionEntity.unsubscribe(entities);
    }
}
