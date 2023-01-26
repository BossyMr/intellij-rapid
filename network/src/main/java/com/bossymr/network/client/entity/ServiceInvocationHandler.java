package com.bossymr.network.client.entity;

import com.bossymr.network.ServiceModel;
import com.bossymr.network.client.HttpNetworkFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull HttpNetworkFactory networkFactory;

    public ServiceInvocationHandler(@NotNull HttpNetworkFactory networkFactory) {
        this.networkFactory = networkFactory;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, ServiceModel.class, "getNetworkFactory")) {
            return networkFactory;
        }
        return networkFactory.getRequestFactory().createQuery(proxy, method, args);
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getInterfaces()[0].getName();
    }
}
