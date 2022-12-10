package com.bossymr.rapid.robot.network.client.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.List;

public record CollectionModel(@NotNull Model model, @NotNull List<Model> models) {

    public static @NotNull CollectionModel getModel(byte @NotNull [] array) throws IOException {
        Document document = getDocument(array);
        Element element = document.getDocumentElement();
        URI path = getPath(element);
        Model entity = Model.getEntityModel(path, element);
        List<Model> entities = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName("li");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element section) {
                entities.add(Model.getModel(path, section));
            }
        }
        return new CollectionModel(entity, entities);
    }

    private static @NotNull URI getPath(@NotNull Element document) throws IOException {
        NodeList nodeList = document.getElementsByTagName("base");
        if (nodeList.getLength() != 1) throw new IOException();
        Node node = nodeList.item(0);
        if (node instanceof Element element) {
            return URI.create(element.getAttribute("href"));
        } else {
            throw new IOException();
        }
    }

    private static @NotNull Document getDocument(byte @NotNull [] array) throws IOException {
        DocumentBuilder builder = getBuilder();
        try {
            return builder.parse(new ByteArrayInputStream(array));
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private static @NotNull DocumentBuilder getBuilder() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        }
    }

    public @Nullable Model model(@NotNull String type) {
        return models.stream()
                .filter(model -> model.type().equals(type))
                .findFirst().orElse(null);
    }

}
