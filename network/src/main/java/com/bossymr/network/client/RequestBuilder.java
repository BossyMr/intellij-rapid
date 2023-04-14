package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.stream.Collectors;

/**
 * A {@code RequestBuilder} is used to build a {@link HttpRequest}.
 */
public class RequestBuilder {

    private final @NotNull URI defaultPath;
    private @NotNull MultiMap<String, String> fields = new MultiMap<>();
    private @NotNull MultiMap<String, String> arguments = new MultiMap<>();
    private @NotNull String method;
    private @NotNull URI path;

    /**
     * Creates a new {@code RequestBuilder} with the specified default path, which all paths are resolved against.
     *
     * @param defaultPath the default path.
     */
    public RequestBuilder(@NotNull URI defaultPath) {
        this.method = "GET";
        this.defaultPath = defaultPath;
        this.path = defaultPath;
    }

    public @NotNull RequestBuilder setMethod(@NotNull String method) {
        this.method = method;
        return this;
    }

    public @NotNull RequestBuilder setPath(@NotNull URI path) {
        this.path = defaultPath.resolve(path);
        return this;
    }

    public @NotNull RequestBuilder setFields(@NotNull MultiMap<String, String> fields) {
        this.fields = fields;
        return this;
    }

    public @NotNull RequestBuilder addField(@NotNull String name, @NotNull String value) {
        fields.add(name, value);
        return this;
    }

    public @NotNull RequestBuilder setField(@NotNull String name, @NotNull String value) {
        fields.set(name, value);
        return this;
    }

    public @NotNull RequestBuilder setArguments(@NotNull MultiMap<String, String> arguments) {
        this.arguments = arguments;
        return this;
    }

    public @NotNull RequestBuilder addArgument(@NotNull String name, @NotNull String value) {
        arguments.add(name, value);
        return this;
    }

    public @NotNull RequestBuilder setArgument(@NotNull String name, @NotNull String value) {
        arguments.set(name, value);
        return this;
    }

    public @NotNull Request build() {
        String body = getBody(fields);
        Request.Builder builder = new Request.Builder()
                .url(getResource(path, arguments).toString());
        RequestBody bodyPublisher = body != null ? RequestBody.create(body.getBytes(), MediaType.get("application/x-www-form-urlencoded")) : null;
        if(method.equals("POST") || method.equals("PUT")) {
            if(bodyPublisher == null) {
                bodyPublisher = RequestBody.create(new byte[0]);
            }
        }
        builder = builder.method(method, bodyPublisher);
        return builder.build();
    }

    private @NotNull URI getResource(@NotNull URI resource, @NotNull MultiMap<String, String> arguments) {
        if (arguments.isEmpty()) {
            return resource;
        }
        String query = resource.getQuery();
        if (query == null) {
            query = "";
        } else {
            query += "&";
        }
        query += arguments.stream()
                .map(entry -> {
                    if (entry.getValue() != null) {
                        return entry.getKey() + "=" + entry.getValue();
                    } else {
                        return entry.getKey();
                    }
                })
                .collect(Collectors.joining("&"));
        try {
            return new URI(resource.getScheme(), resource.getUserInfo(), resource.getHost(), resource.getPort(), resource.getPath(), query, resource.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable String getBody(@NotNull MultiMap<String, String> fields) {
        if (fields.isEmpty()) return null;
        return fields.stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

}
