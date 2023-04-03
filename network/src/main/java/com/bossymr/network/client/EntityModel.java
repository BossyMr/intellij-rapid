package com.bossymr.network.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public record EntityModel(@NotNull String title, @NotNull String type, @NotNull Map<String, URI> references, @NotNull Map<String, String> properties) {

    public static @NotNull Builder newBuilder(@NotNull String title, @NotNull String type) {
        return new Builder(title, type);
    }

    public @Nullable URI reference(@NotNull String type) {
        return references().get(type);
    }

    public @Nullable String property(@NotNull String type) {
        if (type.equals("title")) {
            return title();
        }
        return properties().get(type);
    }

    public static class Builder {

        private @NotNull String title;
        private @NotNull String type;

        private @NotNull Map<String, URI> references;
        private @NotNull Map<String, String> properties;

        public Builder(@NotNull String title, @NotNull String type) {
            this.title = title;
            this.type = type;
            this.references = new HashMap<>();
            this.properties = new HashMap<>();
        }

        public @NotNull Builder setTitle(@NotNull String title) {
            this.title = title;
            return this;
        }

        public @NotNull Builder setType(@NotNull String type) {
            this.type = type;
            return this;
        }

        public @NotNull Builder setReference(@NotNull String type, @NotNull URI reference) {
            this.references.put(type, reference);
            return this;
        }

        public @NotNull Builder setProperty(@NotNull String type, @NotNull String value) {
            this.properties.put(type, value);
            return this;
        }

        public @NotNull Builder setReferences(@NotNull Map<String, URI> references) {
            this.references = new HashMap<>(references);
            return this;
        }

        public @NotNull Builder setProperties(@NotNull Map<String, String> properties) {
            this.properties = new HashMap<>(properties);
            return this;
        }

        public @NotNull EntityModel build() {
            return new EntityModel(title, type, Map.copyOf(references), Map.copyOf(properties));
        }
    }

}
