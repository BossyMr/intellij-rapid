package com.bossymr.network.entity;

import com.bossymr.network.client.NetworkAction;
import com.bossymr.network.client.RequestFactory;
import com.bossymr.network.client.proxy.NetworkProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull Class<?> type;
    private @Nullable NetworkAction action;

    public ServiceInvocationHandler(@NotNull NetworkAction action, @NotNull Class<?> type) {
        this.action = action;
        this.type = type;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, NetworkProxy.class, "getNetworkManager")) {
            return action;
        }
        if (isMethod(method, NetworkProxy.class, "move", NetworkAction.class)) {
            this.action = (NetworkAction) args[0];
            return null;
        }
        if (action == null) {
            throw new IllegalStateException("Entity is not managed");
        }
        return new RequestFactory(action).createQuery(type, proxy, method, args);
    }

    @Override
    public boolean equals(@NotNull Object proxy, @NotNull Object obj) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
        if (!(invocationHandler instanceof ServiceInvocationHandler service)) return false;
        return type.equals(service.type) && Objects.equals(action, service.action);
    }

    @Override
    public int hashCode(@NotNull Object proxy) {
        int result = type.hashCode();
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getInterfaces()[0].getName();
    }
}
