package com.bossymr.rapid.robot.network.client.model;

import com.bossymr.rapid.robot.network.Link;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Model(@NotNull String title, @NotNull String type, @NotNull List<Link> links,
                    @NotNull Map<String, String> properties) {

    public static @NotNull Model getModel(@NotNull URI path, @NotNull Element element) {
        String title = element.getAttribute("title");
        String type = element.getAttribute("class");
        Map<String, String> fields = getFields(element);
        List<Link> links = getLinks(path, element);
        return new Model(title, type, links, fields);
    }

    public static @NotNull Model getEntityModel(@NotNull URI path, @NotNull Element element) throws IOException {
        String title = getTitle(element);
        String type = getType(element);
        Map<String, String> fields = getFields((Element) element.getElementsByTagName("div").item(0));
        List<Link> links = getLinks(path, (Element) element.getElementsByTagName("div").item(0));
        return new Model(title, type, links, fields);
    }

    private static @NotNull List<Element> findElements(@NotNull Element element, @NotNull String type) {
        NodeList nodeList = element.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element child) {
                if (child.getTagName().equals(type)) {
                    elements.add(child);
                }
            }
        }
        return elements;
    }

    private static @NotNull String getTitle(@NotNull Element document) throws IOException {
        NodeList nodeList = document.getElementsByTagName("title");
        if (nodeList.getLength() != 1) throw new IOException();
        Node node = nodeList.item(0);
        return node.getTextContent();
    }

    private static @NotNull String getType(@NotNull Element document) throws IOException {
        NodeList nodeList = document.getElementsByTagName("div");
        if (nodeList.getLength() != 1) throw new IOException();
        Node node = nodeList.item(0);
        if (node instanceof Element element) {
            return element.getAttribute("class");
        }
        throw new IOException();
    }

    private static @NotNull Map<String, String> getFields(@NotNull Element element) {
        Map<String, String> fields = new HashMap<>();
        List<Element> nodeList = findElements(element, "span");
        for (Node node : nodeList) {
            if (node instanceof Element value) {
                String name = value.getAttribute("class");
                String content = value.getTextContent();
                fields.put(name, content);
            }
        }
        return fields;
    }

    private static @NotNull List<Link> getLinks(@NotNull URI path, @NotNull Element element) {
        List<Link> links = new ArrayList<>();
        List<Element> nodeList = findElements(element, "a");
        for (Node node : nodeList) {
            if (node instanceof Element value) {
                links.add(Link.getLink(path, value));
            }
        }
        return links;
    }

    public @Nullable Link getLink(@NotNull String relationship) {
        for (Link link : links()) {
            if (link.relationship().equals(relationship)) {
                return link;
            }
        }
        return null;
    }

    public @Nullable String field(@NotNull String type) {
        return properties().get(type);
    }

}
