package com.bossymr.network.client.model;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollectionModel extends Model {

    private final List<Model> models;

    public CollectionModel(
            @NotNull String title,
            @NotNull String type,
            @NotNull Map<String, String> fields,
            @NotNull Map<String, URI> paths,
            @NotNull List<Model> models
    ) {
        super(title, type, fields, paths);
        this.models = models;
    }

    public static @NotNull CollectionModel convert(byte @NotNull [] response) {
        return new IntermediateConverter().convert(response);

    }

    public @NotNull List<Model> getModels() {
        return models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CollectionModel that = (CollectionModel) o;
        return Objects.equals(getModels(), that.getModels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getModels());
    }

    @Override
    public String toString() {
        return "CollectionModel{" +
                "models=" + getModels() +
                ", title='" + getTitle() + '\'' +
                ", type='" + getType() + '\'' +
                ", fields=" + getFields() +
                ", links=" + getLinks() +
                '}';
    }
}
