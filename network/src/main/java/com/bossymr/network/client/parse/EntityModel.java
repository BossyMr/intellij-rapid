package com.bossymr.network.client.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        getLinks().forEach((type, value) -> buffer.append("<a href=\"").append(value).append("\" rel=\"").append(value).append("\"></a>"));
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
}
