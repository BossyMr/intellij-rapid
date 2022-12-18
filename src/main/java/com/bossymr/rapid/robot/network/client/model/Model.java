package com.bossymr.rapid.robot.network.client.model;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class Model {

    private final String title;
    private final String type;

    private final Map<String, String> fields;
    private final Map<String, URI> paths;

    public Model(@NotNull String title, @NotNull String type, @NotNull Map<String, String> fields, @NotNull Map<String, URI> paths) {
        this.title = title;
        this.type = type;
        this.fields = fields;
        this.paths = paths;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull String getType() {
        return type;
    }

    public @NotNull Map<String, String> getFields() {
        return fields;
    }

    public String getField(@NotNull String type) {
        return getFields().get(type);
    }

    public @NotNull Map<String, URI> getLinks() {
        return paths;
    }

    public URI getLink(@NotNull String type) {
        return getLinks().get(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(getTitle(), model.getTitle()) && Objects.equals(getType(), model.getType()) && Objects.equals(getFields(), model.getFields()) && Objects.equals(paths, model.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getType(), getFields(), paths);
    }

    @Override
    public String toString() {
        return "Model{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", fields=" + fields +
                ", paths=" + paths +
                ", links=" + getLinks() +
                '}';
    }
}
