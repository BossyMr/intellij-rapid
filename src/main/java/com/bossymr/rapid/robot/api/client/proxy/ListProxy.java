package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.NetworkTarget;
import com.bossymr.rapid.robot.api.NetworkType;
import com.bossymr.rapid.robot.api.ResponseStatusException;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class ListProxy<T> extends AbstractList<T> {

    private final @NotNull NetworkManager manager;
    private final @NotNull Class<T> entityType;
    private final @NotNull NetworkTarget<?> target;

    /**
     * The list of elements in this list. A {@code null} element has not yet been loaded.
     */
    private final @NotNull List<T> elements = new ArrayList<>();

    /**
     * The size of a single page. If the page size is less than zero, the page size is unknown.
     */
    private int pageSize = -1;

    /**
     * The upper bound size of this list. The lower bound size is the size of {@code elements}.
     */
    private int sizeUpper = Integer.MAX_VALUE;

    /**
     * Creates a new {@code ListProxy} based on the specified target. The first page of the list will be automatically
     * retrieved.
     *
     * @param manager the manager of this list.
     * @param entityType the type of entity in this list.
     * @param model the response for the first page of the list.
     * @throws ProxyException if an error occurs while creating this proxy.
     */
    public ListProxy(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull ResponseModel model) throws ProxyException {
        this.manager = manager;
        this.entityType = entityType;
        URI self = Objects.requireNonNull(model.getLink("self"));
        this.target = NetworkTarget.newTarget(self, NetworkType.voidType()).build();
        loadPage(0, model);
    }

    private @NotNull List<T> loadPage(int pageStart) throws ProxyException {
        if (pageStart >= sizeUpper) {
            return List.of();
        }
        NetworkTarget.Builder<ResponseModel> builder = NetworkTarget.newTarget(target.getMethod(), target.getPath(), NetworkType.modelType())
                .properties(target.getProperties())
                .argument("start", String.valueOf(pageStart));
        if (pageSize > 0) {
            builder.argument("size", String.valueOf(pageSize));
        }
        NetworkTarget<ResponseModel> target = builder.build();
        ResponseModel model;
        try {
            model = manager.createQuery(target).get();
        } catch (ResponseStatusException e) {
            if (e.getResponse().statusCode() == 400) {
                // The requested page does not exist.
                this.sizeUpper = pageStart;
                return List.of();
            } else {
                throw new ProxyException("could not load page with start '" + pageStart + "'", e);
            }
        } catch (IOException e) {
            throw new ProxyException("could not load page with start '" + pageStart + "'", e);
        } catch (InterruptedException e) {
            throw new ProxyException("the current thread was interrupted", e);
        }
        return loadPage(pageStart, model);
    }

    private @NotNull List<T> loadPage(int pageStart, @NotNull ResponseModel model) throws ProxyException {
        // Try to find the size of a single page, if possible.
        if (pageSize <= 0) {
            URI self = model.getLink("self");
            if (self != null) {
                NetworkTarget.Path path = new NetworkTarget.Path(self);
                try {
                    this.pageSize = Integer.parseInt(path.getArguments().get("size"));
                } catch (NumberFormatException ignored) {}
            }
        }
        int entityIndex = pageStart;
        List<EntityModel> entities = model.getEntities();
        while (elements.size() < (pageStart + entities.size())) {
            elements.add(null);
        }
        for (EntityModel entity : entities) {
            try {
                T managed = manager.createEntity(entityType, entity);
                elements.set(entityIndex, managed);
            } catch (IllegalArgumentException e) {
                throw new ProxyException("could not convert list entity", e);
            }
            entityIndex += 1;
        }
        if (model.getLink("next") == null) {
            this.sizeUpper = pageStart + entities.size();
        }
        return elements.subList(pageStart, pageStart + entities.size());
    }

    private void loadLast() throws ProxyException {
        while (sizeUpper > elements.size()) {
            int pageStart = getPageStart(elements.size());
            loadPage(pageStart);
        }
    }

    private int getPageStart(int index) {
        if (pageSize <= 0) {
            return index;
        }
        return (int) (Math.floor((double) index / pageSize) * pageSize);
    }

    @Override
    public T get(int index) {
        if (sizeUpper >= 0 && index >= sizeUpper) {
            throw new IndexOutOfBoundsException("index '" + index + "' is larger than the upper bound size of the list '" + sizeUpper + "'");
        }
        if (elements.size() > index) {
            T element = elements.get(index);
            if (element != null) {
                return element;
            }
        }
        List<T> result = loadPage(getPageStart(index));
        if (result.isEmpty()) {
            throw new IndexOutOfBoundsException("index '" + index + "' is larger than the size of the list '" + sizeUpper + "'");
        }
        if (elements.size() > index) {
            T element = elements.get(index);
            if (element != null) {
                return element;
            }
            throw new NoSuchElementException("index '" + index + "' could not be loaded");
        }
        throw new IndexOutOfBoundsException("index '" + index + "' is larger than the size of the list '" + sizeUpper + "'");
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public int size() {
        loadLast();
        return elements.size();
    }
}
