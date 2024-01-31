package com.bossymr.network.client;

import com.bossymr.network.GenericType;
import com.bossymr.network.MultiMap;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * A {@code NetworkRequest} represents a request.
 *
 * @param <T> the type of response.
 */
public class NetworkRequest<T> {

    private final @NotNull FetchMethod method;
    private final @NotNull MultiMap<String, String> fields = new MultiMap<>();
    private @NotNull URI path;

    private final @NotNull GenericType<T> type;

    /**
     * Creates a new {@code NetworkRequest} which will send a {@code GET} request to the specified path and convert the
     * response into the specified type.
     *
     * @param path the request path.
     * @param type the respones type.
     */
    public NetworkRequest(@NotNull URI path, @NotNull GenericType<T> type) {
        this(FetchMethod.GET, path, type);
    }

    /**
     * Creates a new {@code NetworkRequest} which will send a request with the specified method and path, and convert
     * the response into the specified type.
     *
     * @param method the request method.
     * @param path the request path.
     * @param type the response type.
     */
    public NetworkRequest(@NotNull FetchMethod method, @NotNull URI path, @NotNull GenericType<T> type) {
        this.method = method;
        this.path = path;
        this.type = type;
    }

    public @NotNull URI getPath() {
        return path;
    }

    public @NotNull GenericType<T> getType() {
        return type;
    }

    @Contract(mutates = "this")
    public @NotNull NetworkRequest<T> setField(@NotNull String name, @Nullable String value) {
        fields.set(name, value);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull NetworkRequest<T> addField(@NotNull String name, @NotNull String value) {
        fields.put(name, value);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull NetworkRequest<T> setArgument(@NotNull String name, @Nullable String value) {
        MultiMap<String, String> arguments = getArguments();
        arguments.set(name, value);
        path = computePath(arguments);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull NetworkRequest<T> addArgument(@NotNull String name, @NotNull String value) {
        MultiMap<String, String> arguments = getArguments();
        arguments.put(name, value);
        path = computePath(arguments);
        return this;
    }

    public @NotNull NetworkRequest<T> putArguments(@NotNull MultiMap<String, String> map) {
        MultiMap<String, String> arguments = getArguments();
        arguments.putAll(map);
        path = computePath(arguments);
        return this;
    }

    private @NotNull URI computePath(@NotNull MultiMap<String, String> arguments) {
        String query = arguments.entrySet().stream()
                .map(entry -> {
                    if (entry.getValue() == null) {
                        return entry.getKey();
                    } else {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
        try {
            return new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), query, path.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public @NotNull FetchMethod getMethod() {
        return method;
    }

    public @NotNull MultiMap<String, String> getFields() {
        return fields;
    }

    public @NotNull MultiMap<String, String> getArguments() {
        String query = path.getQuery();
        MultiMap<String, String> arguments = new MultiMap<>();
        if (query == null || query.isEmpty()) {
            return arguments;
        }
        for (String argument : query.split("&")) {
            String[] strings = argument.split("=");
            if (strings.length != 2) {
                throw new IllegalStateException("Malformed argument: " + argument);
            }
            arguments.put(strings[0], strings[1]);
        }
        return arguments;
    }

    private @Nullable String computeBody(@NotNull MultiMap<String, String> fields) {
        if (fields.isEmpty()) {
            return null;
        }
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    public @NotNull Request build(@NotNull URI defaultPath) {
        URI temporary = defaultPath.resolve(path);
        String body = computeBody(fields);
        Request.Builder builder = new Request.Builder().url(temporary.toString());
        RequestBody bodyPublisher = body != null ? RequestBody.create(body.getBytes(), MediaType.get("application/x-www-form-urlencoded")) : null;
        if (method == FetchMethod.POST || method == FetchMethod.PUT) {
            if (bodyPublisher == null) {
                bodyPublisher = RequestBody.create(new byte[0], MediaType.get("application/x-www-form-urlencoded"));
            }
        }
        builder = builder.method(method.name(), bodyPublisher);
        return builder.build();
    }

    @Override
    public String toString() {
        return "NetworkRequest{" +
                "method=" + method +
                ", path=" + path +
                ", fields=" + fields +
                ", type=" + type +
                '}';
    }
}
