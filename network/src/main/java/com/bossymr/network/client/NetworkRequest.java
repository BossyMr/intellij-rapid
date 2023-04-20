package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NetworkRequest {

    private final @NotNull MultiMap<String, String> fields = new MultiMap<>();
    private final @NotNull MultiMap<String, String> arguments = new MultiMap<>();
    private @NotNull String method;
    private @NotNull URI path;

    public NetworkRequest() {
        this.method = "GET";
        this.path = URI.create("/");
    }

    public @NotNull MultiMap<String, String> getFields() {
        return fields;
    }

    public @NotNull MultiMap<String, String> getArguments() {
        return arguments;
    }

    public @NotNull String getMethod() {
        return method;
    }

    public @NotNull NetworkRequest setMethod(@NotNull String method) {
        this.method = method;
        return this;
    }

    public @NotNull URI getPath() {
        return path;
    }

    public @NotNull NetworkRequest setPath(@NotNull URI path) {
        String query = path.getQuery();
        if (query != null) {
            String[] sections = query.split("&");
            for (String argument : sections) {
                String[] strings = argument.split("=");
                if (strings.length != 2) {
                    throw new IllegalArgumentException("Unexpected query " + argument);
                }
                arguments.putIfAbsent(strings[0], new ArrayList<>());
                if (!(arguments.get(strings[0]).contains(strings[1]))) {
                    arguments.get(strings[0]).add(strings[1]);
                }
            }
        }
        try {
            this.path = new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), null, path.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    public @NotNull NetworkRequest addFields(@NotNull MultiMap<String, String> fields) {
        this.fields.putAll(fields);
        return this;
    }

    public @NotNull NetworkRequest addField(@NotNull String name, @NotNull String value) {
        fields.add(name, value);
        return this;
    }

    public @NotNull NetworkRequest setField(@NotNull String name, @NotNull String value) {
        fields.set(name, value);
        return this;
    }

    public @NotNull NetworkRequest addArguments(@NotNull MultiMap<String, String> arguments) {
        this.arguments.putAll(arguments);
        return this;
    }

    public @NotNull NetworkRequest addArgument(@NotNull String name, @NotNull String value) {
        arguments.add(name, value);
        return this;
    }

    public @NotNull NetworkRequest setArgument(@NotNull String name, @NotNull String value) {
        arguments.set(name, value);
        return this;
    }

    public @NotNull Request build(@NotNull URI defaultPath) {
        path = defaultPath.resolve(path);
        String body = getBody(fields);
        Request.Builder builder = new Request.Builder()
                .url(getResource(path, arguments).toString());
        RequestBody bodyPublisher = body != null ? RequestBody.create(body.getBytes(), MediaType.get("application/x-www-form-urlencoded")) : null;
        if (method.equals("POST") || method.equals("PUT")) {
            if (bodyPublisher == null) {
                bodyPublisher = RequestBody.create(new byte[0], MediaType.get("application/x-www-form-urlencoded"));
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
