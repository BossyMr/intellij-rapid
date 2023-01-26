package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.client.entity.EntityInvocationHandler;
import com.bossymr.network.client.entity.ServiceInvocationHandler;
import com.bossymr.network.client.model.Model;
import com.bossymr.network.client.security.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Supplier;

public class HttpNetworkFactory implements NetworkFactory {

    private final @NotNull URI defaultPath;
    private final @NotNull NetworkClient networkClient;
    private final @NotNull EntityFactory entityFactory;
    private final @NotNull RequestFactory requestFactory;

    public HttpNetworkFactory(@NotNull URI defaultPath, @NotNull Supplier<Credentials> credentials) {
        this.defaultPath = defaultPath;
        this.networkClient = new HttpNetworkClient(defaultPath, credentials);
        this.entityFactory = new EntityFactory(this, networkClient);
        this.requestFactory = new RequestFactory(this);
    }

    public @NotNull EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    public @NotNull RequestFactory getRequestFactory() {
        return requestFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(),
                new Class[]{serviceType},
                new ServiceInvocationHandler(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull Model model) {
        Map<String, Class<? extends T>> arguments = EntityFactory.getEntityType(entityType);
        if (arguments.containsKey(model.getType())) {
            Class<? extends T> returnType = arguments.get(model.getType());
            return (T) Proxy.newProxyInstance(
                    returnType.getClassLoader(),
                    new Class[]{returnType},
                    new EntityInvocationHandler(this, model));
        }
        return null;
    }

    @Override
    public @NotNull RequestBuilder createRequest() {
        return new RequestBuilder(defaultPath);
    }

    @Override
    public @NotNull <T> NetworkCall<T> createNetworkCall(@NotNull HttpRequest request, @NotNull Type returnType) {
        return new HttpNetworkCall<>(request, returnType, this);
    }

    @Override
    public @NotNull <T> SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull SubscribableEvent<T> event) {
        return (priority, listener) -> networkClient.subscribe(event, priority, (entity, model) -> {
            T result = createEntity(event.getEventType(), model);
            if (result != null) {
                listener.onEvent(entity, result);
            }
        });
    }
}
