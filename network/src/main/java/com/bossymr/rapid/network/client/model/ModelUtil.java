package com.bossymr.rapid.network.client.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class ModelUtil {

    private ModelUtil() {}

    public static @NotNull Model createModel(@NotNull URI path, @NotNull InputStream array) throws IOException {
        return createModel(path, new XmlMapper().readTree(array));
    }

    private static Model createModel(@NotNull URI path, @NotNull JsonNode node) {
        String title = node.requiredAt("/head/title").asText();
        JsonNode jsonNode = node.at("/head/base/href");
        path = jsonNode.isMissingNode() ? path : URI.create(jsonNode.asText());
        JsonNode body = node.requiredAt("/body/div");
        String type = body.required("class").asText();
        List<Property> properties = createFields(body.get("span"));
        List<Link> links = createLinks(path, body.get("a"));
        List<EntityModel> entities = createEntities(path, body.path("ul").path("li"));
        EntityModel entity = new EntityModel(title, type, links, properties);
        return new Model(entity, entities);
    }

    private static List<EntityModel> createEntities(@NotNull URI path, @NotNull JsonNode node) {
        List<EntityModel> entities = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode child : node) {
                entities.add(createEntity(path, child));
            }
        }
        if (node.isObject()) {
            entities.add(createEntity(path, node));
        }
        return entities;
    }


    private static EntityModel createEntity(@NotNull URI path, @NotNull JsonNode node) {
        String title = node.required("title").asText();
        String type = node.required("class").asText();
        List<Link> links = createLinks(path, node.get("a"));
        List<Property> properties = createFields(node.get("span"));
        return new EntityModel(title, type, links, properties);
    }

    private static List<Property> createFields(@Nullable JsonNode node) {
        List<Property> properties = new ArrayList<>();
        if (node != null) {
            if (node.isArray()) {
                for (JsonNode child : node) {
                    properties.add(createField(child));
                }
            }
            if (node.isObject()) {
                properties.add(createField(node));
            }
        }
        return properties;
    }

    private static Property createField(@NotNull JsonNode child) {
        String value = child.has("") ? child.get("").asText() : "";
        String name = child.required("class").asText();
        return new Property(name, value);
    }

    private static List<Link> createLinks(@NotNull URI path, @Nullable JsonNode node) {
        List<Link> links = new ArrayList<>();
        if (node != null) {
            if (node.isArray()) {
                for (JsonNode child : node) {
                    links.add(createLink(path, child));
                }
            }
            if (node.isObject()) {
                links.add(createLink(path, node));
            }
        }
        return links;
    }

    private static Link createLink(@NotNull URI path, @NotNull JsonNode node) {
        String relationship = node.required("rel").asText();
        URI address = URI.create(node.required("href").asText());
        return new Link(relationship, path.resolve(address));
    }

}
