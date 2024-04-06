package com.bossymr.network.client.parse;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record ResponseModel(@NotNull EntityModel model,
                            @NotNull List<EntityModel> entities) {

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public static @NotNull Builder newBuilder(@NotNull URI basePath, @NotNull String type) {
        return new Builder(basePath, type);
    }

    public @NotNull String toXML() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">").append("<head><title>");
        buffer.append(model.title());
        buffer.append("</title><base href=\"\"/></head>");
        buffer.append("<body>");
        buffer.append("<div class=\"").append(model.type()).append("\">");
        toXML(buffer, model);
        buffer.append("<ul>");
        for (EntityModel entity : entities) {
            buffer.append("<li class=\"").append(entity.type()).append("\" title=\"").append(entity.title()).append("\">");
            toXML(buffer, entity);
            buffer.append("</li>");
        }
        buffer.append("</ul");
        buffer.append("</div>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    private void toXML(@NotNull StringBuilder buffer, @NotNull EntityModel entity) {
        entity.properties().forEach((type, value) -> buffer.append("<span class=\"").append(type).append("\">").append(value).append("</span>"));
        entity.links().forEach((type, value) -> buffer.append("<a href=\"").append(value).append("\" rel=\"").append(type).append("\"></a>"));
    }

    public static class Builder {

        private final URI basePath;
        private final ResponseModel responseModel;

        public Builder() {
            this(URI.create(""), "state");
        }

        public Builder(@NotNull URI basePath, @NotNull String type) {
            this.basePath = basePath;
            EntityModel entity = new EntityModel("", type, new HashMap<>(), new HashMap<>());
            this.responseModel = new ResponseModel(entity, new ArrayList<>());
        }

        public @NotNull Builder link(@NotNull String type, @NotNull URI link) {
            responseModel.model().links().put(type, basePath.resolve(link));
            return this;
        }

        public @NotNull Builder property(@NotNull String type, @NotNull String value) {
            responseModel.model().properties().put(type, value);
            return this;
        }

        public @NotNull EntityModel.Builder<Builder> entity(@NotNull String title, @NotNull String type) {
            return new EntityModel.Builder<>(basePath, title, type) {
                @Override
                public @NotNull Builder build() {
                    responseModel.entities().add(entityModel);
                    return Builder.this;
                }
            };
        }

        public @NotNull ResponseModel build() {
            return responseModel;
        }
    }
}
