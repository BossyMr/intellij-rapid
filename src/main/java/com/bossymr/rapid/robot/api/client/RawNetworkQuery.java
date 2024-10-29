package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.MultiMap;
import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A network query that returns the response.
 */
public class RawNetworkQuery implements NetworkQuery<HttpResponse<byte[]>> {

    private final NetworkClient client;

    private final RequestMethod method;
    private final NetworkPath path;
    private final MultiMap<String, String> properties = new MultiMap<>();

    private RawNetworkQuery(@NotNull NetworkClient client, @NotNull RequestMethod method, @NotNull URI path) {
        this.client = client;
        this.method = method;
        this.path = new NetworkPath(path);
    }

    @Override
    public @NotNull URI getPath() {
        return path.getPath();
    }

    @Override
    public GenericType<HttpResponse<byte[]>> getType() {
        return new GenericType<>() {};
    }

    public RequestMethod getMethod() {
        return method;
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
        String body = getBody();
        HttpRequest.BodyPublisher bodyPublisher;
        if (body != null) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        }
        HttpRequest request = HttpRequest.newBuilder(getPath())
                .method(method.name(), bodyPublisher)
                .build();
        return client.send(request);
    }

    /**
     * A builder for a {@link RawNetworkQuery}.
     * <p>
     * A builder is obtained with {@link NetworkClient#newRequest(URI)} or
     * {@link NetworkClient#newRequest(RequestMethod, URI)}.
     */
    public static class Builder {

        private final RawNetworkQuery query;

        protected Builder(@NotNull NetworkClient client, @NotNull URI path) {
            this(client, RequestMethod.GET, path);
        }

        protected Builder(@NotNull NetworkClient client, @NotNull RequestMethod method, @NotNull URI path) {
            this.query = new RawNetworkQuery(client, method, path);
        }

        /**
         * Adds a property to this query.
         *
         * @param key the name of the property.
         * @param value the value of the property.
         * @return this builder.
         */
        public @NotNull Builder property(@NotNull String key, @NotNull String value) {
            this.query.getProperties().put(key, value);
            return this;
        }

        /**
         * Adds an argument to the path of this query.
         *
         * @param key the name of the argument.
         * @param value the value of the argument.
         * @return this builder.
         */
        public @NotNull Builder argument(@NotNull String key, @NotNull String value) {
            this.query.getArguments().put(key, value);
            return this;
        }

        /**
         * Adds all the provided properties to this query.
         *
         * @param properties the properties.
         * @return this builder.
         */
        public @NotNull Builder properties(@NotNull Map<String, String> properties) {
            this.query.getProperties().putAll(properties);
            return this;
        }

        /**
         * Adds all the provided arguments to the path of this query.
         *
         * @param arguments the arguments.
         * @return this builder.
         */
        public @NotNull Builder arguments(@NotNull Map<String, String> arguments) {
            this.query.getArguments().putAll(arguments);
            return this;
        }

        /**
         * Builds and returns a {@code NetworkQuery}.
         *
         * @return a new {@code NetworkQuery}.
         */
        public @NotNull NetworkQuery<HttpResponse<byte[]>> build() {
            RawNetworkQuery copy = new RawNetworkQuery(query.client, query.method, query.path.getPath());
            copy.getProperties().putAll(query.properties);
            return copy;
        }
    }
}
