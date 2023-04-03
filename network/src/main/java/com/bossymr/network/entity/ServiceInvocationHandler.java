package com.bossymr.network.entity;

import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.RequestFactory;
import com.bossymr.network.client.proxy.NetworkProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull Class<?> type;
    private @Nullable NetworkManager manager;

    public ServiceInvocationHandler(@NotNull NetworkManager manager, @NotNull Class<?> type) {
        this.manager = manager;
        this.type = type;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, NetworkProxy.class, "getNetworkManager")) {
            return manager;
        }
        if (isMethod(method, NetworkProxy.class, "move", NetworkManager.class)) {
            this.manager = (NetworkManager) args[0];
            return null;
        }
        if (manager == null) {
            throw new IllegalStateException("Entity is not managed");
        }
        return new RequestFactory(manager).createQuery(type, proxy, method, args);
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getInterfaces()[0].getName();
    }
}
