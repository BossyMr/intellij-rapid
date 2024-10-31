package com.bossymr.rapid.robot.api.client.entity;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ResponseModel extends EntityModel {

    private static final Logger log = LoggerFactory.getLogger(ResponseModel.class);

    private final List<EntityModel> entities;

    public ResponseModel() {
        super("state", "");
        this.entities = new ArrayList<>();
    }

    public ResponseModel(@NotNull String type, @NotNull String title) {
        super(type, title);
        this.entities = new ArrayList<>();
    }

    public static @NotNull Builder newBuilder(@NotNull String type, @NotNull String title) {
        return new Builder(type, title);
    }

    public @NotNull List<EntityModel> getEntities() {
        return entities;
    }

    public static @NotNull ResponseModel fromXML(@NotNull String text) {
        Document element = Jsoup.parse(text);
        Element typeElement = element.selectFirst("html > body > div");
        String type = typeElement != null ? typeElement.className() : "";
        Element titleElement = element.selectFirst("html > head > title");
        String title = titleElement != null ? titleElement.text() : "";
        Element defaultPathElement = element.selectFirst("html > head > base[href]");
        URI defaultPath = URI.create(defaultPathElement != null ? defaultPathElement.attr("href") : "");
        ResponseModel model = new ResponseModel(type, title);
        if (typeElement != null) {
            fromXML(model, defaultPath, typeElement);
            for (Element node : typeElement.select("ul > li")) {
                EntityModel entity = EntityModel.fromXML(defaultPath, node);
                model.getEntities().add(entity);
            }
        }
        return model;
    }

    @Override
    public @NotNull String toXML() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">").append("<head><title>");
        buffer.append(getTitle());
        buffer.append("</title><base href=\"\"/></head>");
        buffer.append("<body>");
        buffer.append("<div class=\"").append(getType()).append("\">");
        writeXML(buffer);
        buffer.append("<ul>");
        for (EntityModel entity : entities) {
            buffer.append(entity.toXML());
        }
        buffer.append("</ul>");
        buffer.append("</div>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResponseModel that = (ResponseModel) o;
        return Objects.equals(entities, that.entities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), entities);
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "type='" + getType() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", links=" + getLinks() +
                ", properties=" + getProperties() +
                ", entities=" + getEntities() +
                '}';
    }

    public static class Builder extends EntityModel.Builder {

        public Builder(@NotNull String type, @NotNull String title) {
            super(new ResponseModel(type, title));
        }

        @Override
        public @NotNull Builder property(@NotNull String key, @NotNull String value) {
            super.property(key, value);
            return this;
        }

        @Override
        public @NotNull Builder properties(@NotNull Map<String, String> properties) {
            super.properties(properties);
            return this;
        }

        @Override
        public @NotNull Builder link(@NotNull String key, @NotNull URI value) {
            super.link(key, value);
            return this;
        }

        @Override
        public @NotNull Builder links(@NotNull Map<String, URI> links) {
            super.links(links);
            return this;
        }

        public @NotNull Builder entity(@NotNull EntityModel entity) {
            ((ResponseModel) model).getEntities().add(entity);
            return this;
        }

        public @NotNull Builder entity(@NotNull String type, @NotNull String title, @NotNull Consumer<EntityModel.Builder> consumer) {
            EntityModel.Builder builder = EntityModel.newBuilder(type, title);
            consumer.accept(builder);
            EntityModel entity = builder.build();
            ((ResponseModel) model).getEntities().add(entity);
            return this;
        }

        public @NotNull Builder entities(@NotNull List<EntityModel> entities) {
            ((ResponseModel) model).getEntities().addAll(entities);
            return this;
        }

        @Override
        public @NotNull ResponseModel build() {
            return ((ResponseModel) super.build());
        }
    }
}
