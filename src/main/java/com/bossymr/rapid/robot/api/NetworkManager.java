package com.bossymr.rapid.robot.api;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Property;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.client.NetworkClient;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.proxy.NetworkProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.api.entity.EntityInvocationHandler;
import com.bossymr.rapid.robot.api.entity.ServiceInvocationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@code NetworkManager} is connected to a remote server, and can create and manage entities and services.
 * <p>
 * An entity or service is an instance of an interface. A {@link Service service} has no state and can only contain
 * {@link Fetch requests}. An {@link Entity entity}, however, represents a resource, and can contain either requests or
 * methods which return a specific {@link Property property} of the entity.
 * <p>
 * An entity can be either managed or unmanaged. A service must always be managed. A managed entity will send all
 * requests to the {@code NetworkManager} managing it. However, an unmanaged entity will throw a {@link ProxyException}
 * if a request is invoked.
 */
public class NetworkManager implements AutoCloseable {

    protected final NetworkClient networkClient;
    private final @NotNull Set<Listener> listeners = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    /**
     * Create a new {@code NetworkManager}.
     *
     * @param defaultPath the path to resolve requests with.
     * @param credentials the credentials to authenticate requests with.
     */
    public NetworkManager(@NotNull URI defaultPath, @Nullable Credentials credentials) {
        this.networkClient = new NetworkClient(defaultPath, credentials);
    }

    /**
     * Creates a new unmanaged entity.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity.
     * @throws IllegalArgumentException if the entity model could not be deserialized into the specified entity type, or
     * if the specified type is annotated as an {@link Entity entity}.
     */
    public static <T> @NotNull T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        return EntityInvocationHandler.createEntity(null, entityType, model);
    }

    /**
     * Creates a new group. The new group is a child of this manager and will be closed if this manager is closed.
     *
     * @return a new group.
     */
    public @NotNull Group group() {
        Group group = new Group();
        subscribe(new Listener() {
            @Override
            public void onClose() throws IOException, InterruptedException {
                group.close();
            }
        });
        return group;
    }

    /**
     * Creates a copy of the specified entity or service which uses this {@code NetworkManager}.
     *
     * @param entity the entity or service.
     * @param <T> the entity or service type.
     * @return a new copy of the specified entity or service.
     * @throws IllegalArgumentException if the specified object doesn't represent an entity or service.
     */
    public <T> @NotNull T move(@NotNull T entity) {
        if (!(entity instanceof NetworkProxy proxy)) {
            throw new IllegalArgumentException("Argument '" + entity + "' doesn't represent an @Entity or @Service");
        }
        proxy.attach(this);
        return entity;
    }

    /**
     * Returns the {@code NetworkClient} used by this {@code NetworkManager} to handle network communication.
     *
     * @return the {@code NetworkClient} used by this {@code NetworkManager}.
     */
    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    /**
     * Creates a new {@code NetworkQuery} that can be used to send a request to the specified target.
     *
     * @param target the target.
     * @param <T> the response type.
     * @return the query.
     */
    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull NetworkTarget<T> target) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return () -> {
            NetworkType<T> type = target.getType();
            HttpResponse<byte[]> response = getNetworkClient().send(target);
            return type.convert(this, response);
        };
    }

    /**
     * Creates a new {@code SubscribableNetworkQuery} that can be used to subscribe to the specified target.
     *
     * @param event the event.
     * @param <T> the event type.
     * @return the query
     */
    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableTarget<T> event) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return (priority, listener) -> getNetworkClient().subscribe(event, priority, new SubscriptionListener<>() {
            @Override
            public void onEvent(@NotNull SubscriptionEntity entity, @NotNull EntityModel response) {
                T result = createEntity(event.getType(), response);
                listener.onEvent(entity, result);
            }

            @Override
            public void onClose(@NotNull SubscriptionEntity entity) {
                listener.onClose(entity);
            }
        });
    }

    /**
     * Creates a new service managed by this {@code NetworkManager}.
     *
     * @param serviceType the service type.
     * @param <T> the service type.
     * @return the service
     * @throws IllegalArgumentException if the specified type is not annotated with {@link Service}.
     */
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) throws IllegalArgumentException {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return ServiceInvocationHandler.createService(this, serviceType);
    }

    /**
     * Creates a new entity managed by this {@code NetworkManager}.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity
     * @throws IllegalArgumentException if the provided model could not be converted into an entity of the specified
     * type, or if the specified type is not annotated with {@link Entity}.
     */
    public <T> @NotNull T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return EntityInvocationHandler.createEntity(this, entityType, model);
    }

    /**
     * Subscribes to the state of this {@code NetworkManager}.
     *
     * @param listener the event listener.
     */
    public void subscribe(@NotNull Listener listener) {
        listeners.add(listener);
    }

    /**
     * Close this {@code NetworkManager} and any ongoing subscriptions.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        for (Listener listener : listeners) {
            listener.onClose();
        }
        closed = true;
        networkClient.close();
    }

    /**
     * A listener that listens to the state of a {@code NetworkManager}.
     */
    public interface Listener {
        /**
         * Called when a {@code NetworkManager} is closed.
         *
         * @throws IOException if an I/O error occurs.
         * @throws InterruptedException if the current thread is interrupted.
         */
        default void onClose() throws IOException, InterruptedException {}
    }

    /**
     * A {@code Group} is used to group together subscriptions. A {@code Group} can be closed, which will close all
     * grouped subscriptions.
     */
    public static class Group implements AutoCloseable {

        private final List<SubscriptionEntity> subscriptions = new ArrayList<>();
        private final List<Group> children = new ArrayList<>();

        /**
         * Collects the provided subscription into this group.
         *
         * @param entity a subscription.
         */
        public void collect(SubscriptionEntity entity) {
            subscriptions.add(entity);
        }

        /**
         * Creates a new group. The new group is a child of this group. As a result, if this group is closed, the new
         * group will also be closed.
         *
         * @return a new group.
         */
        public Group group() {
            Group group = new Group();
            children.add(group);
            return group;
        }

        @Override
        public void close() throws IOException, InterruptedException {
            for (Group group : children) {
                group.close();
            }
            children.clear();
            for (SubscriptionEntity entity : subscriptions) {
                entity.unsubscribe();
            }
            subscriptions.clear();
        }
    }
}
