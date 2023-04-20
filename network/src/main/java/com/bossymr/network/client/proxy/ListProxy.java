package com.bossymr.network.client.proxy;

import com.bossymr.network.NetworkManager;
import com.bossymr.network.NetworkQuery;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class ListProxy<T> extends AbstractList<T> {

    private final @NotNull NetworkManager manager;
    private final @NotNull Class<T> entityType;
    private final @NotNull NetworkRequest request;

    private List<List<T>> sections;

    public ListProxy(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull NetworkRequest request) {
        this.manager = manager;
        this.entityType = entityType;
        this.request = request;
    }

    private void build() {
        ResponseModel model = getModel(request);
        this.sections = new ArrayList<>();

        this.sections.add(createElements(model));
        URI next;
        while ((next = model.model().reference("next")) != null) {
            NetworkRequest copy = new NetworkRequest()
                    .setMethod(request.getMethod())
                    .addFields(request.getFields())
                    .setPath(next);
            model = getModel(copy);
            this.sections.add(createElements(model));
        }
    }

    private @NotNull List<T> createElements(@NotNull ResponseModel response) {
        return response.entities().stream()
                .map(entity -> {
                    try {
                        return manager.createEntity(entityType, entity);
                    } catch (IllegalArgumentException e) {
                        // Skip entities which could not converted.
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private @NotNull ResponseModel getModel(@NotNull NetworkRequest request) {
        NetworkQuery<ResponseModel> query = manager.createQuery(ResponseModel.class, request);
        try {
            ResponseModel model = query.get();
            if (model == null) {
                throw new ProxyException("Could not evaluate response '" + request + "'");
            }
            return model;
        } catch (IOException e) {
            throw new ProxyException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProxyException(e);
        }
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     * @throws ProxyException if the element at the specified position could not be retrieved.
     */
    @Override
    public T get(int index) {
        if (sections == null) {
            build();
        }
        for (int i = 0, j = 0; i < sections.size(); i++) {
            List<T> block = sections.get(i);
            if (index < (j + block.size())) {
                return block.get(index - j);
            }
            j += block.size();
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the number of elements in this list. Fetches the first page of the response, if it has not already been
     * retrieved.
     *
     * @return the number of elements in this list.
     * @throws ProxyException if the number of elements in this list could not be retrieved.
     */
    @Override
    public int size() {
        if (sections == null) {
            build();
        }
        return sections.stream().mapToInt(List::size).sum();
    }
}
