package com.bossymr.rapid.robot.api.client.response;

import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.ResponseConverter;
import com.bossymr.rapid.robot.api.ResponseConverterFactory;
import com.bossymr.rapid.robot.api.client.EntityModel;
import com.bossymr.rapid.robot.api.client.ResponseModel;
import okhttp3.Response;
import org.apache.tika.utils.XMLReaderUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseModelConverter implements ResponseConverter<ResponseModel> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull GenericType<T> type) {
            if (type.getRawType().equals(ResponseModel.class)) {
                return (ResponseConverter<T>) new ResponseModelConverter();
            }
            return null;
        }
    };

    private final DocumentBuilder builder;

    public ResponseModelConverter() {
        DocumentBuilderFactory builderFactory = XMLReaderUtils.getDocumentBuilderFactory();
        try {
            this.builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable ResponseModel convert(@NotNull Response response) throws IOException {
        byte[] body = response.body().bytes();
        response.close();
        return convert(body);
    }

    public @NotNull ResponseModel convert(byte @NotNull [] body) {
        Document document = parse(body);
        Element element = document.getDocumentElement();
        String title = findChild(element, "title").getTextContent();
        List<Element> sections = findChildren(element, "div");
        Element section = sections.size() == 1 ? sections.get(0) : getSection(sections);
        String type = section.getAttribute("class");
        URI defaultPath = getDefaultPath(element);
        Map<String, URI> links = getLinks(defaultPath, section);
        Map<String, String> fields = getFields(section);
        List<EntityModel> models = List.copyOf(getModels(defaultPath, element));
        return new ResponseModel(new EntityModel(title, type, links, fields), models);
    }

    private @NotNull Element getSection(@NotNull List<Element> elements) {
        for (Element element : elements) {
            if (element.getAttribute("class").equals("status")) {
                return element;
            }
        }
        return elements.get(0);
    }

    private @NotNull List<EntityModel> getModels(@NotNull URI defaultPath, @NotNull Element element) {
        List<Element> elements = findChildren(element, "li");
        List<EntityModel> models = new ArrayList<>();
        for (Element model : elements) {
            String title = model.getAttribute("title");
            String type = model.getAttribute("class");
            Map<String, URI> links = getLinks(defaultPath, model);
            Map<String, String> fields = getFields(model);
            models.add(new EntityModel(title, type, links, fields));
        }
        return models;
    }

    private @NotNull Map<String, URI> getLinks(@NotNull URI defaultPath, @NotNull Element element) {
        List<Element> elements = getChildren(element, "a");
        Map<String, URI> links = new HashMap<>();
        for (Element link : elements) {
            links.put(link.getAttribute("rel"), defaultPath.resolve(link.getAttribute("href")));
        }
        return links;
    }

    private @NotNull Map<String, String> getFields(@NotNull Element element) {
        List<Element> elements = getChildren(element, "span");
        Map<String, String> fields = new HashMap<>();
        for (Element field : elements) {
            fields.put(field.getAttribute("class"), field.getTextContent());
        }
        return fields;
    }

    private @NotNull URI getDefaultPath(@NotNull Element document) {
        Element element = findChild(document, "base");
        String attribute = element.getAttribute("href");
        return URI.create(attribute);
    }

    private @NotNull Element findChild(@NotNull Element element, @NotNull String type) {
        List<Element> elements = findChildren(element, type);
        assert elements.size() == 1;
        return elements.get(0);
    }

    private @NotNull List<Element> getChildren(@NotNull Element element, @NotNull String type) {
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

    private @NotNull List<Element> findChildren(@NotNull Element element, @NotNull String type) {
        NodeList nodeList = element.getElementsByTagName(type);
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element child) {
                elements.add(child);
            }
        }
        return elements;
    }

    private @NotNull Document parse(byte @NotNull [] body) {
        try {
            String input = new String(body, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(input);
            input = matcher.replaceAll(result -> result.group()
                                                       .replaceAll("<", "&lt;")
                                                       .replaceAll(">", "&gt;"));
            return builder.parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(new String(body), e);
        }
    }
}
