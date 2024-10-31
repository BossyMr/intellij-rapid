package com.bossymr.rapid.robot.api.entity;

import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.client.proxy.NetworkProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public abstract class AbstractInvocationHandler implements InvocationHandler {

    protected @Nullable NetworkManager manager;

    public static boolean isMethod(@NotNull Method method, @NotNull Class<?> declaringClass, @NotNull String name, @NotNull Class<?>... parameters) {
        return method.getName().equals(name) &&
                method.getDeclaringClass().equals(declaringClass) &&
                Arrays.equals(method.getParameterTypes(), parameters);
    }

    @Override
    public @Nullable Object invoke(@NotNull Object proxy, @NotNull Method method, Object @Nullable [] args) throws Throwable {
        args = args != null ? args : new Object[0];
        if (isMethod(method, Object.class, "hashCode")) {
            return hashCode(proxy);
        }
        if (isMethod(method, Object.class, "equals", Object.class)) {
            return equals(proxy, args[0]);
        }
        if (isMethod(method, Object.class, "toString")) {
            return toString(proxy);
        }
        if (isMethod(method, NetworkProxy.class, "getNetworkAction")) {
            return manager;
        }
        if (isMethod(method, NetworkProxy.class, "attach", NetworkManager.class)) {
            this.manager = (NetworkManager) args[0];
            return null;
        }
        if (isMethod(method, NetworkProxy.class, "detach", NetworkManager.class)) {
            this.manager = null;
            return null;
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        return execute(proxy, method, args);
    }

    public abstract @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable;

    public int hashCode(@NotNull Object proxy) {
        return Proxy.getInvocationHandler(proxy).hashCode();
    }

    public boolean equals(@NotNull Object proxy, @NotNull Object obj) {
        return proxy == obj;
    }

    public String toString(@NotNull Object proxy) {
        return proxy.getClass().getName() + "@" + Integer.toHexString(hashCode(proxy));
    }
}
