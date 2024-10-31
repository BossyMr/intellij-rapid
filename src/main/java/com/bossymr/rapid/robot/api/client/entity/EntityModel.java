package com.bossymr.rapid.robot.api.client.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityModel {

    private final String type, title;

    private final Map<String, URI> links;
    private final Map<String, String> properties;

    public EntityModel(@NotNull String type, @NotNull String title) {
        this.type = type;
        this.title = title;
        this.links = new HashMap<>();
        this.properties = new HashMap<>();
    }

    public static @NotNull Builder newBuilder(@NotNull String type, @NotNull String title) {
        return new Builder(new EntityModel(type, title));
    }

    public static @NotNull EntityModel fromXML(@NotNull String text) {
        Document document = Jsoup.parse(text);
        return fromXML(URI.create(""), document);
    }

    public static @NotNull EntityModel fromXML(@NotNull URI defaultPath, @NotNull Element element) {
        String type = element.className();
        String title = element.attr("title");
        EntityModel model = new EntityModel(type, title);
        fromXML(model, defaultPath, element);
        return model;
    }

    protected static void fromXML(@NotNull EntityModel model, @NotNull URI defaultPath, @NotNull Element element) {
        for (Element link : element.select(":root > a")) {
            String linkType = link.attr("rel");
            URI value = defaultPath.resolve(URI.create(link.attr("href")));
            model.getLinks().put(linkType, value);
        }
        for (Element property : element.select(":root > span")) {
            String propertyType = property.className();
            String value = property.text();
            model.getProperties().put(propertyType, value);
        }
    }

    public @NotNull String getType() {
        return type;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public @NotNull Map<String, URI> getLinks() {
        return links;
    }

    public @Nullable URI getLink(@NotNull String type) {
        return links.get(type);
    }

    public @NotNull Map<String, String> getProperties() {
        return properties;
    }

    public @Nullable String getProperty(@NotNull String type) {
        return properties.get(type);
    }

    public @NotNull String toXML() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<li class=\"").append(type).append("\" title=\"").append(title).append("\">");
        writeXML(buffer);
        buffer.append("</li>");
        return buffer.toString();
    }

    protected void writeXML(@NotNull StringBuilder buffer) {
        getLinks().forEach((type, value) -> buffer.append("<a rel=\"").append(type).append("\" href=\"").append(value).append("\"></a>"));
        getProperties().forEach((type, value) -> buffer.append("<span class=\"").append(type).append("\">").append(value).append("</span>"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityModel that = (EntityModel) o;
        return Objects.equals(type, that.type) && Objects.equals(title, that.title) && Objects.equals(links, that.links) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, links, properties);
    }

    @Override
    public String toString() {
        return "EntityModel{" +
               "type='" + getType() + '\'' +
               ", title='" + getTitle() + '\'' +
               ", links=" + getLinks() +
               ", properties=" + getProperties() +
               '}';
    }

    public static class Builder {

        protected final EntityModel model;

        protected Builder(@NotNull EntityModel model) {
            this.model = model;
        }

        public @NotNull Builder property(@NotNull String key, @NotNull String value) {
            model.getProperties().put(key, value);
            return this;
        }

        public @NotNull Builder properties(@NotNull Map<String, String> properties) {
            model.getProperties().putAll(properties);
            return this;
        }

        public @NotNull Builder link(@NotNull String key, @NotNull URI value) {
            model.getLinks().put(key, value);
            return this;
        }

        public @NotNull Builder links(@NotNull Map<String, URI> links) {
            model.getLinks().putAll(links);
            return this;
        }

        public @NotNull EntityModel build() {
            return model;
        }
    }
}
