package com.bossymr.rapid.robot.network.client.model;

import com.bossymr.rapid.robot.network.client.annotations.Entity;
import com.bossymr.rapid.robot.network.client.annotations.Field;
import com.bossymr.rapid.robot.network.client.annotations.Title;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ModelFactory {

    private static final Logger LOG = Logger.getInstance(ModelFactory.class);

    private final ObjectMapper objectMapper;
    private final URI path;

    public ModelFactory(URI path) {
        this.objectMapper = new XmlMapper();
        this.path = path;
    }

    public <T> @NotNull T getEntity(@NotNull Response response, @NotNull Class<T> clazz) throws IOException {
        return getEntity(getModel(response), clazz);
    }

    public <T> @NotNull T getEntity(@NotNull Model model, @NotNull Class<T> clazz) {
        if (model.entities().size() != 1) throw new IllegalStateException();
        EntityModel entity = model.entities().get(0);
        if (entity.type().endsWith("-li")) throw new IllegalStateException();
        if (!canHandle(entity, clazz)) throw new IllegalStateException();
        return getEntity(entity, clazz);
    }

    public <T> @NotNull List<T> getList(@NotNull Model model, @NotNull Class<T> clazz) {
        if (model.entities().size() == 1) {
            EntityModel entity = model.entities().get(0);
            if (!(entity.type().endsWith("-li"))) {
                throw new IllegalStateException();
            }
        }
        List<T> entities = new ArrayList<>();
        for (EntityModel entity : model.entities()) {
            if (canHandle(entity, clazz)) {
                entities.add(getEntity(entity, clazz));
            }
        }
        return Collections.unmodifiableList(entities);
    }

    public <T> @NotNull T getEntity(@NotNull EntityModel entity, @NotNull Class<T> clazz) {
        assert clazz.isRecord();
        assert canHandle(entity, clazz);
        RecordComponent[] components = clazz.getRecordComponents();
        Class<?>[] constructorType = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            constructorType[i] = component.getType();
            if (component.isAnnotationPresent(Field.class)) {
                Field field = component.getAnnotation(Field.class);
                Optional<Property> optional = entity.getProperty(field.value());
                if (optional.isPresent()) {
                    String value = optional.get().content();
                    JavaType javaType = objectMapper.constructType(component.getGenericType());
                    Object argument = objectMapper.convertValue(value, javaType);
                    arguments[i] = argument;
                } else {
                    arguments[i] = null;
                }
            } else if(component.isAnnotationPresent(Title.class)) {
                String value = entity.title();
                JavaType javaType = objectMapper.constructType(component.getGenericType());
                Object argument = objectMapper.convertValue(value, javaType);
                arguments[i] = argument;
            }
        }
        try {
            return clazz.getDeclaredConstructor(constructorType).newInstance(arguments);
        } catch (Exception e) {
            LOG.error("Failed to convert " + entity + " into " + clazz);
            throw new IllegalStateException();
        }
    }

    private boolean canHandle(@NotNull EntityModel entity, @NotNull Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) throw new IllegalStateException();
        Entity annotation = clazz.getAnnotation(Entity.class);
        for (String expression : annotation.value()) {
            if (canHandle(expression, entity.type())) {
                return true;
            }
        }
        return false;
    }

    private boolean canHandle(@NotNull String expression, @NotNull String value) {
        return Pattern.compile(expression).matcher(value).matches();
    }

    public @NotNull Model getModel(@NotNull Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            return getModel(responseBody);
        } else {
            throw new IllegalStateException(response.message());
        }
    }

    public @NotNull Model getModel(@NotNull ResponseBody responseBody) throws IOException {
        JsonNode node = objectMapper.readTree(responseBody.byteStream());
        return createModel(node);
    }

    private Model createModel(@NotNull JsonNode node) {
        String title = node.requiredAt("/head/title").asText();
        JsonNode jsonNode = node.at("/head/base/href");
        URI path = jsonNode.isMissingNode() ? this.path : URI.create(jsonNode.asText());
        JsonNode body = node.requiredAt("/body/div");
        String type = body.required("class").asText();
        List<Property> properties = createFields(body.get("span"));
        List<Link> links = createLinks(path, body.get("a"));
        List<EntityModel> entities = createEntities(path, body.path("ul").path("li"));
        EntityModel entity = new EntityModel(title, type, links, properties);
        return new Model(entity, entities);
    }

    private List<EntityModel> createEntities(@NotNull URI path, @NotNull JsonNode node) {
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


    private EntityModel createEntity(@NotNull URI path, @NotNull JsonNode node) {
        String title = node.required("title").asText();
        String type = node.required("class").asText();
        List<Link> links = createLinks(path, node.get("a"));
        List<Property> properties = createFields(node.get("span"));
        return new EntityModel(title, type, links, properties);
    }

    private List<Property> createFields(@Nullable JsonNode node) {
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

    private Property createField(@NotNull JsonNode child) {
        String value = child.has("") ? child.get("").asText() : "";
        String name = child.required("class").asText();
        return new Property(name, value);
    }

    private List<Link> createLinks(@NotNull URI path, @Nullable JsonNode node) {
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

    private Link createLink(@NotNull URI path, @NotNull JsonNode node) {
        String relationship = node.required("rel").asText();
        URI address = URI.create(node.required("href").asText());
        return new Link(relationship, path.resolve(address));
    }

}
