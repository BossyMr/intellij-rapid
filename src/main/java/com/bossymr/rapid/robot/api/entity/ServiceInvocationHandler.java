package com.bossymr.rapid.robot.api.entity;

import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.client.RequestFactory;
import com.bossymr.rapid.robot.api.client.proxy.NetworkProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ServiceInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull Class<?> type;

    public ServiceInvocationHandler(@NotNull NetworkManager manager, @NotNull Class<?> type) {
        this.manager = manager;
        this.type = type;
    }

    public static <T> @NotNull T createService(@NotNull NetworkManager manager, @NotNull Class<T> serviceType) {
        Class<? extends T> virtualType = new ByteBuddy()
                .subclass(serviceType)
                .implement(NetworkProxy.class)
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(new ServiceInvocationHandler(manager, serviceType)))
                .make()
                .load(NetworkManager.class.getClassLoader())
                .getLoaded();
        try {
            Constructor<? extends T> constructor = virtualType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("could not create service: service '" + serviceType + "' must define a constructor with no arguments", e);
        }
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, NetworkProxy.class, "getNetworkManager")) {
            return manager;
        }
        if (manager == null) {
            throw new ProxyException("could not invoke method '" + method.getName() + "': service is not managed");
        }
        return new RequestFactory(manager).createQuery(type, proxy, method, args);
    }

    @Override
    public boolean equals(@NotNull Object proxy, @NotNull Object obj) {
        if (!(obj instanceof NetworkProxy service)) return false;
        if (!(type.isInstance(obj))) return false;
        return Objects.equals(manager, service.getNetworkManager());
    }

    @Override
    public int hashCode(@NotNull Object proxy) {
        int result = type.hashCode();
        result = 31 * result + (manager != null ? manager.hashCode() : 0);
        return result;
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getInterfaces()[0].getName();
    }
}
