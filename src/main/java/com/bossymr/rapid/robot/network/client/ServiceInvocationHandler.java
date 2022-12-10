package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.annotations.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final NetworkClient networkClient;
    private final Class<?> serviceType;

    public ServiceInvocationHandler(@NotNull Class<?> serviceType, @NotNull NetworkClient networkClient) {
        assert serviceType.isAnnotationPresent(Service.class) : "ServiceInvocationHandler cannot be created for proxy '" + serviceType.getName() + "' - method not annotated as service.";
        this.serviceType = serviceType;
        this.networkClient = networkClient;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        assert serviceType.isInstance(proxy);
        return NetworkUtil.newQuery(serviceType, networkClient, proxy, method, args);
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return serviceType.getName();
    }
}
