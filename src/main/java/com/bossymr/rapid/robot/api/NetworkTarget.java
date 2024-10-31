package com.bossymr.rapid.robot.api;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A {@code NetworkTarget} represents a target to which a network request can be sent.
 *
 * @param <T> the return type of the target.
 */
public class NetworkTarget<T> {

    private final @NotNull RequestMethod method;
    private final @NotNull Path path;
    private final @NotNull NetworkType<T> type;
    private final @NotNull MultiMap<String, String> properties = new MultiMap<>();

    /**
     * Creates a new {@code NetworkTarget} referencing a queryable resource at the provided path.
     *
     * @param method the request method.
     * @param path the path to the resource.
     * @param type the return type of the query.
     */
    public NetworkTarget(@NotNull RequestMethod method, @NotNull URI path, @NotNull NetworkType<T> type) {
        this.method = method;
        this.path = new Path(path);
        this.type = type;
    }

    /**
     * Creates a new {@code NetworkTarget} builder.
     *
     * @param path the request path.
     * @param type the request type.
     * @param <T> the request type.
     * @return a new builder.
     */
    public static <T> @NotNull Builder<T> newTarget(@NotNull URI path, @NotNull NetworkType<T> type) {
        return new Builder<>(RequestMethod.GET, path, type);
    }

    /**
     * Creates a new {@code NetworkTarget} builder.
     *
     * @param path the request path.
     * @param <T> the request type.
     * @return a new builder.
     */
    public static <T> @NotNull Builder<T> newTarget(@NotNull RequestMethod method, @NotNull URI path, @NotNull NetworkType<T> type) {
        return new Builder<>(method, path, type);
    }

    /**
     * {@return the request method}
     */
    public @NotNull RequestMethod getMethod() {
        return method;
    }

    /**
     * {@return the path to the resource}
     */
    public @NotNull URI getPath() {
        return path.getPath();
    }

    /**
     * {@return the return type}
     */
    public @NotNull NetworkType<T> getType() {
        return type;
    }

    /**
     * {@return the request body}
     */
    public @NotNull MultiMap<String, String> getProperties() {
        return properties;
    }

    /**
     * A builder for a {@link NetworkTarget}.
     * <p>
     * A builder is obtained with {@link NetworkTarget#newTarget(URI, NetworkType)} or
     * {@link NetworkTarget#newTarget(RequestMethod, URI, NetworkType)}.
     */
    public static class Builder<T> {

        private final NetworkTarget<T> target;

        private Builder(@NotNull URI path, @NotNull NetworkType<T> type) {
            this(RequestMethod.GET, path, type);
        }

        private Builder(@NotNull RequestMethod method, @NotNull URI path, @NotNull NetworkType<T> type) {
            this.target = new NetworkTarget<>(method, path, type);
        }

        /**
         * Adds a property to this query.
         *
         * @param key the name of the property.
         * @param value the value of the property.
         * @return this builder.
         */
        public @NotNull Builder<T> property(@NotNull String key, @NotNull String value) {
            this.target.properties.put(key, value);
            return this;
        }

        /**
         * Adds all the provided properties to this query.
         *
         * @param properties the properties.
         * @return this builder.
         */
        public @NotNull Builder<T> properties(@NotNull Map<String, String> properties) {
            this.target.properties.putAll(properties);
            return this;
        }

        /**
         * Adds an argument to the path of this query.
         *
         * @param key the name of the argument.
         * @param value the value of the argument.
         * @return this builder.
         */
        public @NotNull Builder<T> argument(@NotNull String key, @NotNull String value) {
            this.target.path.getArguments().put(key, value);
            return this;
        }

        /**
         * Adds all the provided arguments to the path of this query.
         *
         * @param arguments the arguments.
         * @return this builder.
         */
        public @NotNull Builder<T> arguments(@NotNull Map<String, String> arguments) {
            this.target.path.getArguments().putAll(arguments);
            return this;
        }

        /**
         * Builds and returns a {@code NetworkTarget}.
         *
         * @return a new {@code NetworkTarget}.
         */
        public @NotNull NetworkTarget<T> build() {
            return target;
        }
    }

    /**
     * A {@code Path} represents a path but adds support for easily modifying query arguments.
     */
    public static class Path {

        private final URI path;
        private final MultiMap<String, String> arguments = new MultiMap<>();

        public Path(@NotNull URI path) {
            String query = path.getQuery();
            // Check if the path has any query arguments.
            if (query == null || query.isEmpty()) {
                this.path = path;
                return;
            }
            for (String argument : query.split("&")) {
                String[] strings = argument.split("=");
                if (strings.length == 1) {
                    arguments.put(strings[0].strip(), null);
                }
                String value = argument.substring(strings[0].length() + 1);
                arguments.put(strings[0].strip(), value);
            }
            try {
                this.path = new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), null, path.getFragment());
            } catch (URISyntaxException e) {
                // This should never throw an exception since we are only removing the query part of the path.
                throw new IllegalArgumentException("unreachable statement" + path);
            }
        }

        public @NotNull MultiMap<String, String> getArguments() {
            return arguments;
        }

        public @NotNull URI getPath() {
            StringJoiner query = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                if (entry.getValue() == null) {
                    query.add(entry.getKey());
                } else {
                    query.add(entry.getKey() + "=" + entry.getValue());
                }
            }
            try {
                return new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), query.toString(), path.getFragment());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("URI is invalid: " + path);
            }
        }

        @Override
        public String toString() {
            URI path = getPath();
            return path.toString();
        }
    }
}
