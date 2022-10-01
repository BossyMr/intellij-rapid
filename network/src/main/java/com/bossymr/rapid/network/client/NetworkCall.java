package com.bossymr.rapid.network.client;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.*;

public class NetworkCall {

    private final URI path;
    private final NetworkMethod method;

    private final Map<String, List<String>> queries;
    private final Map<String, List<String>> fields;

    private NetworkCall(Builder builder) {
        this.path = builder.path;
        this.method = builder.method;
        this.queries = Collections.unmodifiableMap(builder.queries);
        this.fields = Collections.unmodifiableMap(builder.fields);
    }

    public @NotNull URI getPath() {
        return path;
    }

    public @NotNull NetworkMethod getMethod() {
        return method;
    }

    public @NotNull Map<String, List<String>> getQueries() {
        return queries;
    }

    public @NotNull Map<String, List<String>> getFields() {
        return fields;
    }

    public static @NotNull Builder newBuilder() {
        return new Builder(URI.create(""));
    }

    public static @NotNull Builder newBuilder(@NotNull URI path) {
        return new Builder(path);
    }

    public static class Builder {

        private URI path;
        private NetworkMethod method;
        private final Map<String, List<String>> queries;
        private final Map<String, List<String>> fields;

        private Builder(@NotNull URI path) {
            this.path = path;
            this.method = NetworkMethod.GET;
            this.queries = new HashMap<>();
            this.fields = new HashMap<>();
        }

        public @NotNull Builder setPath(@NotNull URI path) {
            this.path = path;
            return this;
        }


        public @NotNull Builder setMethod(@NotNull NetworkMethod method) {
            this.method = method;
            return this;
        }


        public @NotNull Builder putQuery(@NotNull String name, @NotNull String value) {
            this.queries.computeIfAbsent(name, (k) -> new ArrayList<>());
            this.queries.get(name).add(value);
            return this;
        }

        public @NotNull Builder putField(@NotNull String name, @NotNull String value) {
            this.fields.computeIfAbsent(name, (k) -> new ArrayList<>());
            this.fields.get(name).add(value);
            return this;
        }

        public @NotNull Builder putQueries(@NotNull Map<String, String> queries) {
            queries.forEach(this::putQuery);
            return this;
        }

        public @NotNull Builder putFields(@NotNull Map<String, String> fields) {
            fields.forEach(this::putField);
            return this;
        }

        public @NotNull NetworkCall build() {
            return new NetworkCall(this);
        }
    }

}
