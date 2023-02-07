package com.bossymr.network.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CollectionModelBuilder extends ModelBuilder<CollectionModel> {

    private final List<Model> models = new ArrayList<>();

    public @NotNull ModelBuilder<CollectionModelBuilder> addModel() {
        return new ModelBuilder<>() {
            @Override
            public @NotNull CollectionModelBuilder build() {
                Model model = new Model(title, type, fields, links);
                CollectionModelBuilder.this.models.add(model);
                return CollectionModelBuilder.this;
            }
        };
    }

    @Override
    public @NotNull CollectionModel build() {
        return new CollectionModel(title, type, fields, links, models);
    }
}
