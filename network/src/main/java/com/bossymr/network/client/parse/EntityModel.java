package com.bossymr.network.client.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public record EntityModel(@NotNull String title,
                          @NotNull String type,
                          @NotNull Map<String, URI> links,
                          @NotNull Map<String, String> properties) {

    public static @NotNull Builder<EntityModel> newBuilder(@NotNull String title, @NotNull String type) {
        return new Builder<>(title, type) {
            @Override
            public @NotNull EntityModel build() {
                return entityModel;
            }
        };
    }

    public @Nullable URI link(@NotNull String type) {
        return links.get(type);
    }

    public @Nullable String property(@NotNull String type) {
        return properties.get(type);
    }

    public abstract static class Builder<T> {

        protected final EntityModel entityModel;
        private final URI basePath;

        public Builder(@NotNull String title, @NotNull String type) {
            this(URI.create(""), title, type);
        }

        public Builder(@NotNull URI basePath, @NotNull String title, @NotNull String type) {
            this.basePath = basePath;
            this.entityModel = new EntityModel(title, type, new HashMap<>(), new HashMap<>());
        }

        public @NotNull Builder<T> link(@NotNull String type, @NotNull URI link) {
            entityModel.links().put(type, basePath.resolve(link));
            return this;
        }

        public @NotNull Builder<T> property(@NotNull String type, @NotNull String value) {
            entityModel.properties().put(type, value);
            return this;
        }

        public abstract @NotNull T build();

    }

}
