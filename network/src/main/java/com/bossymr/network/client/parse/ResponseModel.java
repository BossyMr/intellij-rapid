package com.bossymr.network.client.parse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResponseModel extends EntityModel {

    private final List<EntityModel> entities;

    public ResponseModel() {
        super("state", "");
        this.entities = new ArrayList<>();
    }

    public ResponseModel(@NotNull String type, @NotNull String title) {
        super(type, title);
        this.entities = new ArrayList<>();
    }

    public @NotNull List<EntityModel> getEntities() {
        return entities;
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
        buffer.append("</ul");
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
}
