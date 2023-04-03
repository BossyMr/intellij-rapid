package com.bossymr.network.client.proxy;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * A {@code List} proxy with support for retrieving elements lazily.
 * <p>
 * A response with multiple elements is divided into pages, where each page contains a number of elements. A page is as
 * a {@link Block}, which contains a set number of entities. If an entity is retrieved, and its containing block is
 * already retried, it is immediately returned, otherwise, its containing is retrieved.
 *
 * @param <T> the type of elements in this list.
 */
public class ListProxy<T> extends AbstractList<T> {

    private final @NotNull NetworkManager manager;
    private final @NotNull Class<T> entityType;
    private final @NotNull HttpRequest request;

    private BlockList<T> sections;

    public ListProxy(@NotNull NetworkManager manager, @NotNull Class<T> entityType, @NotNull HttpRequest request) {
        this.manager = manager;
        this.entityType = entityType;
        this.request = request;
    }

    private @NotNull ResponseModel getModel(@NotNull HttpRequest request) {
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
            sections = new BlockList<>(manager, entityType, getModel(request));
        }
        for (int i = 0, j = 0; i < sections.size(); i++) {
            Block<T> block = sections.get(i);
            if (index < (j + block.size())) {
                if (block.get(0) != null) {
                    return block.get(index - j);
                } else {
                    URI path = request.uri();
                    Map<String, String> query = Arrays.stream(path.getQuery().split("&amp;|&"))
                            .map(value -> value.split("="))
                            .collect(Collectors.toMap(value -> value[0], value -> value[1]));
                    query.put("start", String.valueOf(j));
                    query.put("limit", String.valueOf(block.size()));
                    String collected = query.entrySet().stream().map(value -> value.getKey() + "=" + value.getValue()).collect(Collectors.joining("&"));
                    try {
                        path = new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), collected, path.getFragment());
                        block.fill(getModel(HttpRequest.newBuilder(request, (key, value) -> true).uri(path).build()));
                        return block.get(index - j);
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
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
            sections = new BlockList<>(manager, entityType, getModel(request));
        }
        return sections.stream().mapToInt(Block::size).sum();
    }
}
