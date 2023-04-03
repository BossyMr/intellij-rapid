package com.bossymr.network.client.proxy;

import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.AbstractList;

public class Block<T> extends AbstractList<T> {

    private final @NotNull NetworkManager manager;
    private final @NotNull Class<T> entityType;
    private final @Nullable T @NotNull [] elements;

    @SuppressWarnings("unchecked")
    public Block(@NotNull NetworkManager manager, @NotNull Class<T> entityType, int length) {
        this.manager = manager;
        this.entityType = entityType;
        this.elements = (T[]) Array.newInstance(entityType, length);
    }

    public Block(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull ResponseModel response) {
        this(manager, entityType, response.entities().size());
        fill(response);
    }

    public void fill(@NotNull ResponseModel response) {
        for (int i = 0; i < response.entities().size(); i++) {
            EntityModel entity = response.entities().get(i);
            elements[i] = manager.createEntity(entityType, entity);
        }
    }

    @Override
    public T get(int index) {
        return elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }
}
