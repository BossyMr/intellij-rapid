package com.bossymr.network.model;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class ModelBuilder<T> {

    protected final Map<String, String> fields = new HashMap<>();
    protected final Map<String, URI> links = new HashMap<>();

    protected String title = "";
    protected String type = "";

    public @NotNull ModelBuilder<T> setType(@NotNull String type) {
        this.type = type;
        return this;
    }

    public @NotNull ModelBuilder<T> setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    public @NotNull ModelBuilder<T> addField(@NotNull String name, @NotNull String value) {
        fields.put(name, value);
        return this;
    }

    public @NotNull ModelBuilder<T> addLink(@NotNull String name, @NotNull String value) {
        links.put(name, URI.create(value));
        return this;
    }

    public abstract @NotNull T build();

}
