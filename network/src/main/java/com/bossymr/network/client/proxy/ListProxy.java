package com.bossymr.network.client.proxy;

import com.bossymr.network.GenericType;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.NetworkQuery;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class ListProxy<T> extends AbstractList<T> {

    private final @NotNull List<List<T>> sections;

    public ListProxy(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull NetworkRequest<?> request) throws IOException, InterruptedException {
        this.sections = build(manager, entityType, request);
    }

    private static <T> @NotNull List<List<T>> build(@NotNull NetworkManager manager, @NotNull Class<T> type, @NotNull NetworkRequest<?> request) throws IOException, InterruptedException {
        NetworkRequest<ResponseModel> modelCopy = new NetworkRequest<>(request.getMethod(), request.getPath(), GenericType.of(ResponseModel.class));
        modelCopy.getFields().putAll(request.getFields());
        ResponseModel model = getModel(manager, modelCopy);
        List<List<T>> sections = new ArrayList<>();
        sections.add(createElements(manager, type, model));
        URI next;
        while ((next = model.model().reference("next")) != null) {
            NetworkRequest<ResponseModel> copy = new NetworkRequest<>(request.getMethod(), next, GenericType.of(ResponseModel.class));
            copy.getFields().putAll(request.getFields());
            model = getModel(manager, copy);
            sections.add(createElements(manager, type, model));
        }
        return sections;
    }

    private static <T> @NotNull List<T> createElements(@NotNull NetworkManager manager, @NotNull Class<T> type, @NotNull ResponseModel response) {
        return response.entities().stream()
                       .map(entity -> {
                           try {
                               return manager.createEntity(type, entity);
                           } catch (IllegalArgumentException e) {
                               // Skip entities which could not converted.
                               return null;
                           }
                       })
                       .filter(Objects::nonNull)
                       .toList();
    }

    private static @NotNull ResponseModel getModel(@NotNull NetworkManager manager, @NotNull NetworkRequest<ResponseModel> request) throws IOException, InterruptedException {
        NetworkQuery<ResponseModel> query = manager.createQuery(request);
        ResponseModel model = query.get();
        if (model == null) {
            throw new ProxyException("Could not evaluate response '" + request + "'");
        }
        return model;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     */
    @Override
    public T get(int index) {
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
     */
    @Override
    public int size() {
        return sections.stream().mapToInt(List::size).sum();
    }
}
