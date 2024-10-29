package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ListProxy<T> extends AbstractList<T> {

    private final @NotNull List<List<T>> sections;

    public ListProxy(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull NetworkQuery<HttpResponse<byte[]>> request) throws IOException, InterruptedException {
        this.sections = build(manager, entityType, request);
    }

    private static <T> @NotNull List<List<T>> build(@NotNull NetworkManager manager, @NotNull Class<T> type, @NotNull NetworkQuery<HttpResponse<byte[]>> request) throws IOException, InterruptedException {
        ResponseModel model = getModel(request.map(response -> ResponseModel.fromXML(new String(response.body(), StandardCharsets.UTF_8))));
        List<List<T>> sections = new ArrayList<>();
        sections.add(createElements(manager, type, model));
        URI next;
        while ((next = model.getLink("next")) != null) {
            NetworkQuery<ResponseModel> query = manager.getNetworkClient().newRequest(next).build()
                    .map(response -> ResponseModel.fromXML(new String(response.body(), StandardCharsets.UTF_8)));
            model = getModel(query);
            sections.add(createElements(manager, type, model));
        }
        return sections;
    }

    private static <T> @NotNull List<T> createElements(@NotNull NetworkManager manager, @NotNull Class<T> type, @NotNull ResponseModel response) {
        return response.getEntities().stream()
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

    private static @NotNull ResponseModel getModel(@NotNull NetworkQuery<ResponseModel> request) throws IOException, InterruptedException {
        ResponseModel model = request.get();
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
