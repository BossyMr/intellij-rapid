package com.bossymr.network.client;

import com.bossymr.network.GenericType;
import com.bossymr.network.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class RawNetworkQuery<T> implements NetworkQuery<HttpResponse<byte[]>> {

    private final NetworkClient client;

    private final RequestMethod method;
    private final NetworkPath path;
    private final MultiMap<String, String> properties = new MultiMap<>();
    private final GenericType<T> type;

    public RawNetworkQuery(@NotNull NetworkClient client, @NotNull URI path, @NotNull GenericType<T> type) {
        this(client, RequestMethod.GET, path, type);
    }

    public RawNetworkQuery(@NotNull NetworkClient client, @NotNull RequestMethod method, @NotNull URI path, @NotNull GenericType<T> type) {
        this.client = client;
        this.method = method;
        this.type = type;
        this.path = new NetworkPath(path);
    }

    public @NotNull URI getPath() {
        return path.getPath();
    }

    public @NotNull GenericType<T> getType() {
        return type;
    }

    public @NotNull MultiMap<String, String> getProperties() {
        return properties;
    }

    public @NotNull MultiMap<String, String> getArguments() {
        return path.getArguments();
    }

    private @Nullable String getBody() {
        if (properties.isEmpty()) {
            return null;
        }
        return properties.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    @Override
    public @NotNull HttpResponse<byte[]> get() throws IOException, InterruptedException {
        URI path = client.getBasePath().resolve(getPath());
        String body = getBody();
        HttpRequest.BodyPublisher bodyPublisher;
        if (body != null) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        }
        HttpRequest request = HttpRequest.newBuilder(path)
                .method(method.name(), bodyPublisher)
                .build();
        return client.send(request);
    }
}
