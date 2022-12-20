package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.model.ModelUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EntityInvocationHandler extends AbstractInvocationHandler {

    private final NetworkClient networkClient;
    private final Class<?> entityType;
    private Model model;

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
        if (isMethod(method, EntityModel.class, "getLink", String.class)) {
            return getLink((String) args[0]);
        }
        if (isMethod(method, EntityModel.class, "getField", String.class)) {
            return getField((String) args[0]);
        }
        if (method.isAnnotationPresent(Property.class)) {
            Property property = method.getAnnotation(Property.class);
            String name = property.value();
            String value = getField(name);
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

    private URI getLink(@NotNull String relationship) {
        URI link = model.getLink(relationship);
        if (link != null) return link;
        if (fetch()) {
            return model.getLink(relationship);
        }
        return null;
    }

    private String getField(@NotNull String type) {
        String field = model.getField(type);
        if (field != null) return field;
        if (fetch()) {
            return model.getField(type);
        }
        return null;
    }

    private boolean fetch() {
        if (model.getType().endsWith("-li")) {
            if (model.getLink("self") != null) {
                HttpRequest httpRequest = HttpRequest.newBuilder(model.getLink("self")).build();
                try {
                    HttpResponse<byte[]> response = networkClient.send(httpRequest);
                    CollectionModel collectionModel = ModelUtil.convert(response.body());
                    if (collectionModel.getModels().size() != 1) return false;
                    Model complete = collectionModel.getModels().get(0);
                    if (complete.getType().equals(model.getType().substring(0, model.getType().length() - 3))) {
                        model = complete;
                        return true;
                    }
                } catch (IOException | InterruptedException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return entityType.getCanonicalName() + ":" + model;
    }
}
