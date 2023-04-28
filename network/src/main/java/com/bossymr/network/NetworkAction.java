package com.bossymr.network;

import com.bossymr.network.client.*;
import com.bossymr.network.client.response.EntityConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAction implements NetworkManager, TrackableNetworkManager {

    private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();
    private final @NotNull NetworkManager manager;
    private final @NotNull Set<NetworkAction> delegates = ConcurrentHashMap.newKeySet();

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
        if (!(manager instanceof TrackableNetworkManager trackable)) {
            throw new IllegalArgumentException("Cannot create NetworkAction with: " + manager);
        }
        trackable.track(this);
    }


    /**
     * This method is called for each successful response.
     *
     * @param request the request.
     * @param entity the entity.
     * @param <T> the entity type.
     * @return whether to elevate this response.
     * @throws IOException if an I/O error has occured.
     */
    protected <T> boolean onSuccess(@NotNull NetworkRequest request, @Nullable T entity) throws IOException {
        return true;
    }

    /**
     * This method is called for each unsuccessful response.
     *
     * @param request the request.
     * @param throwable the exception.
     * @return whether to elevate this response.
     * @throws IOException if an I/O error has occured.
     */
    protected boolean onFailure(@NotNull NetworkRequest request, @NotNull Throwable throwable) throws IOException, InterruptedException {
        return true;
    }

    @Override
    public @NotNull NetworkClient getNetworkClient() {
        return manager.getNetworkClient();
    }

    @Override
    public void track(@NotNull NetworkAction action) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        delegates.add(action);
    }

    @Override
    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull GenericType<T> type, @NotNull NetworkRequest request) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        NetworkQuery<T> query = HeavyNetworkManager.createQuery(this, type, request);
        return () -> {
            try {
                T response = query.get();
                NetworkManager entity = this;
                while (entity instanceof NetworkAction action) {
                    if (!(onSuccess(request, response))) {
                        break;
                    }
                    entity = action.manager;
                }
                return response;
            } catch (IOException e) {
                NetworkManager entity = this;
                while (entity instanceof NetworkAction action) {
                    if (!(onFailure(request, e))) {
                        break;
                    }
                    entity = action.manager;
                }
                throw e;
            }
        };
    }

    @Override
    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> event) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return (priority, listener) -> {
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
        };
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
        closed = true;
        for (NetworkAction manager : delegates) {
            manager.close();
        }
        SubscriptionEntity.unsubscribe(entities);
    }

    public static class ShutdownOnFailure extends NetworkAction {

        public ShutdownOnFailure(@NotNull NetworkManager manager) {
            super(manager);
        }

        @Override
        protected boolean onFailure(@NotNull NetworkRequest request, @NotNull Throwable throwable) throws IOException, InterruptedException {
            close();
            // The exception message might be refined by another NetworkAction
            // Alternatively, a UI-notification might be displayed
            return true;
        }
    }
}
