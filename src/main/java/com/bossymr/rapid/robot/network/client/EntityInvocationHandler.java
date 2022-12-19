package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class EntityInvocationHandler extends AbstractInvocationHandler {

    private final NetworkClient networkClient;
    private final Model model;
    private final Class<?> entityType;

    public EntityInvocationHandler(@NotNull Class<?> entityType, @Nullable NetworkClient networkClient, @NotNull Model model) {
        assert entityType.isAnnotationPresent(Entity.class) : "EntityInvocationHandler cannot be created for proxy '" + entityType.getName() + "' - method not annotated as entity.";
        assert !entityType.isAnnotationPresent(Service.class) : "EntityInvocationHandler cannot be created for proxy '" + entityType.getName() + "' - method annotated as service.";
        this.entityType = entityType;
        this.networkClient = networkClient;
        this.model = model;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, EntityModel.class, "getTitle")) {
            return model.getTitle();
        }
        if (isMethod(method, EntityModel.class, "getType")) {
            return model.getType();
        }
        if (isMethod(method, EntityModel.class, "getLinks")) {
            return model.getLinks();
        }
        if (isMethod(method, EntityModel.class, "getFields")) {
            return model.getFields();
        }
        if (isMethod(method, EntityModel.class, "getNetworkClient")) {
            return networkClient;
        }
        if (method.isAnnotationPresent(Property.class)) {
            Property property = method.getAnnotation(Property.class);
            String name = property.value();
            String value = model.getField(name);
            if (value != null) {
                return NetworkUtil.convert(value, method.getReturnType());
            } else {
                return null;
            }
        }
        if (networkClient != null) {
            return NetworkUtil.newQuery(entityType, networkClient, proxy, method, args);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return entityType.getName() + ":" + model;
    }
}
