package com.bossymr.rapid.robot.network.client.model;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of utility methods used to convert a response body into a {@link CollectionModel}.
 */
public final class ModelUtil {

    private ModelUtil() {
        throw new AssertionError();
    }

    private static @NotNull DocumentBuilder getBuilder() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // This shouldn't be called, as factory is not dynamically configured.
            throw new RuntimeException(e);
        }
    }

    public static @NotNull CollectionModel convert(byte @NotNull [] response) throws IOException {
        Document document = getDocument(response);
        Element element = document.getDocumentElement();
        String title = getElement(element, "title").getTextContent();
        List<Element> sections = getElements(element, "div");
        Element section = sections.size() == 1 ? sections.get(0) : getSection(sections);
        String type = section.getAttribute("class");
        URI defaultPath = getDefaultPath(element);
        Map<String, URI> links = getLinks(defaultPath, section);
        Map<String, String> fields = getFields(section);
        List<Model> models = getModels(defaultPath, element);
        return new CollectionModel(title, type, fields, links, models);
    }

    private static @NotNull Element getSection(@NotNull List<Element> elements) {
        for (Element element : elements) {
            if (element.getAttribute("class").equals("status")) {
                return element;
            }
        }
        return elements.get(0);
    }

    private static @NotNull List<Model> getModels(@NotNull URI defaultPath, @NotNull Element element) {
        List<Element> elements = getElements(element, "li");
        List<Model> models = new ArrayList<>();
        for (Element model : elements) {
            String title = model.getAttribute("title");
            String type = model.getAttribute("class");
            Map<String, URI> links = getLinks(defaultPath, model);
            Map<String, String> fields = getFields(model);
            models.add(new Model(title, type, fields, links));
        }
        return models;
    }

    private static @NotNull Map<String, URI> getLinks(@NotNull URI defaultPath, @NotNull Element element) {
        List<Element> elements = getStrictElements(element, "a");
        Map<String, URI> links = new HashMap<>();
        for (Element link : elements) {
            links.put(link.getAttribute("rel"), defaultPath.resolve(link.getAttribute("href")));
        }
        return links;
    }

    private static @NotNull Map<String, String> getFields(@NotNull Element element) {
        List<Element> elements = getStrictElements(element, "span");
        Map<String, String> fields = new HashMap<>();
        for (Element field : elements) {
            fields.put(field.getAttribute("class"), field.getTextContent());
        }
        return fields;
    }

    private static @NotNull URI getDefaultPath(@NotNull Element document) {
        Element element = getElement(document, "base");
        String attribute = element.getAttribute("href");
        return URI.create(attribute);
    }

    private static @NotNull Element getElement(@NotNull Element element, @NotNull String tag) {
        List<Element> elements = getElements(element, tag);
        if (elements.size() != 1) {
            throw new IllegalArgumentException("'" + element + "' contains multiple nodes of type '" + tag + "'");
        }
        return elements.get(0);
    }

    private static @NotNull List<Element> getStrictElements(@NotNull Element element, @NotNull String tag) {
        NodeList nodeList = element.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element child) {
                if (child.getTagName().equals(tag)) {
                    elements.add(child);
                }
            }
        }
        return elements;
    }

    private static @NotNull List<Element> getElements(@NotNull Element element, @NotNull String tag) {
        NodeList nodeList = element.getElementsByTagName(tag);
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element child) {
                elements.add(child);
            }
        }
        return elements;
    }

    private static @NotNull Document getDocument(byte @NotNull [] response) throws IOException {
        try {
            return getBuilder().parse(new ByteArrayInputStream(new String(response).getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            System.out.println(new String(response));
            throw new IOException(e);
        }
    }
}
