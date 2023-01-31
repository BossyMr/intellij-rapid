package com.bossymr.network.entity;

import com.bossymr.network.ServiceModel;
import com.bossymr.network.client.NetworkEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull NetworkEngine engine;

    public ServiceInvocationHandler(@NotNull NetworkEngine networkFactory) {
        this.engine = networkFactory;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, ServiceModel.class, "getNetworkEngine")) {
            return engine;
        }
        return engine.getRequestFactory().createQuery(proxy, method, args);
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getInterfaces()[0].getName();
    }
}
