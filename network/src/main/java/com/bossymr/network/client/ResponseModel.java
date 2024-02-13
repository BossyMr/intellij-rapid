package com.bossymr.network.client;

import com.bossymr.network.client.response.ResponseModelConverter;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ResponseModel(@NotNull EntityModel model, @NotNull List<EntityModel> entities) {

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull ResponseModel convert(byte @NotNull [] body) {
        return new ResponseModelConverter().convert(body);
    }

    public @NotNull String title() {
        return model().title();
    }

    public @NotNull String type() {
        return model().type();
    }

    public @NotNull String toText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        stringBuilder.append("<html>");
        stringBuilder.append("<head>")
                .append("<title>").append(title()).append("</title>")
                .append("<base href=\"").append("\"/>")
                .append("</head>");
        stringBuilder.append("<body>");
        stringBuilder.append("<div class=\"").append(type()).append("\">");
        appendEntity(stringBuilder, model());
        stringBuilder.append("<ul>");
        for (EntityModel model : entities()) {
            stringBuilder.append("<li class=\"").append(model.type()).append("\" title=\"").append(model.title()).append("\">");
            appendEntity(stringBuilder, model);
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        stringBuilder.append("</div>");
        stringBuilder.append("</body>");
        stringBuilder.append("</html>");
        return stringBuilder.toString();
    }

    private void appendEntity(@NotNull StringBuilder stringBuilder, @NotNull EntityModel model) {
        model.properties().forEach((type, value) -> {
            stringBuilder.append("<span class=\"").append(type).append("\">");
            stringBuilder.append(value).append("</span>");
        });
        model.references().forEach((type, link) -> stringBuilder.append("<a href=\"").append(link).append("\" rel=\"").append(type).append("\"> </a>"));
    }

    public static class Builder {

        private @NotNull String title;
        private @NotNull String type;

        private @NotNull Map<String, URI> references;
        private @NotNull Map<String, String> properties;

        private @NotNull List<EntityModel> entities;

        public Builder() {
            this.title = "";
            this.type = "state";
            this.references = new HashMap<>();
            this.properties = new HashMap<>();
            this.entities = new ArrayList<>();
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

        public @NotNull Builder setEntity(@NotNull EntityModel entity) {
            this.entities.add(entity);
            return this;
        }

        public @NotNull Builder setEntities(@NotNull List<EntityModel> entities) {
            this.entities = new ArrayList<>(entities);
            return this;
        }

        public @NotNull ResponseModel build() {
            EntityModel entityModel = new EntityModel(title, type, Map.copyOf(references), Map.copyOf(properties));
            return new ResponseModel(entityModel, entities);
        }
    }

}
