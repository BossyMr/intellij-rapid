package com.bossymr.rapid.robot.network.impl;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.RobotState.SymbolState;
import com.bossymr.rapid.robot.network.Controller;
import com.bossymr.rapid.robot.network.security.Authenticator;
import com.bossymr.rapid.robot.network.security.impl.DigestAuthenticator;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class ControllerImpl implements Controller {

    private static final Logger LOG = Logger.getInstance(Controller.class);

    private static final CookieManager COOKIE_MANAGER = new CookieManager();


    private final Authenticator authenticator;
    private final HttpClient httpClient;
    private final URI path;

    public ControllerImpl(@NotNull URI path, @NotNull Credentials credentials) {
        this.path = path;
        this.authenticator = new DigestAuthenticator(() -> credentials);
        COOKIE_MANAGER.getCookieStore().removeAll();
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(COOKIE_MANAGER)
                .build();
    }

    protected @NotNull CollectionModel send(@NotNull HttpRequest request) throws IOException {
        HttpResponse<InputStream> response = retry(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 300) {
            throw ResponseStatusException.of(response);
        }
        return CollectionModel.getModel(response.body());
    }

    protected <T> @NotNull HttpResponse<T> retry(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException {
        HttpRequest authenticated = authenticator.authenticate(request);
        try {
            HttpResponse<T> response = notify(authenticated != null ? authenticated : request, bodyHandler);
            if (response.statusCode() == 401 || response.statusCode() == 407) {
                HttpRequest retry = authenticator.authenticate(response);
                if (retry != null) {
                    return notify(retry, bodyHandler);
                }
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    protected <T> @NotNull HttpResponse<T> notify(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpResponse<T> response = httpClient.send(request, bodyHandler);
        LOG.info("Request: " + request + " with response: " + response);
        return response;
    }

    @Override
    public @NotNull URI getPath() {
        return path;
    }

    @Override
    public @NotNull String getName() throws IOException {
        HttpRequest request = HttpRequest.newBuilder(path.resolve("/ctrl/identity")).GET().build();
        CollectionModel collectionModel = send(request);
        assert collectionModel.models().size() == 1;
        Model model = collectionModel.models().get(0);
        assert model.type().startsWith("ctrl-identity-info");
        return Objects.requireNonNull(model.field("ctrl-name"));
    }

    @Override
    public @NotNull Set<SymbolState> getSymbols() throws IOException {
        String arguments = "symtyp=any&recursive=true";
        HttpRequest request = HttpRequest.newBuilder(path.resolve("/rw/rapid/symbols?action=search-symbols"))
                .POST(HttpRequest.BodyPublishers.ofString(arguments))
                .setHeader("Content-Type", "application/x-www-form-urlencoded").build();
        CollectionModel collectionModel = send(request);
        List<Model> models = new ArrayList<>();
        Link link;
        while ((link = collectionModel.model().link("next")) != null) {
            models.addAll(collectionModel.models());
            HttpRequest next = HttpRequest.newBuilder(request, (n, v) -> true).uri(link.path()).build();
            collectionModel = send(next);
        }
        Set<SymbolState> states = new HashSet<>();
        for (Model model : models) {
            if (model.type().startsWith("rap-symprop")) {
                states.add(getSymbolState(model));
            }
        }
        Map<String, Set<String>> symbols = new HashMap<>();
        for (SymbolState state : states) {
            // Example: "" (contains MoveJ), "MoveJ" (contains arguments of MoveJ)
            String title = state.title.substring("RAPID".length(), state.title.lastIndexOf('/'));
            if (title.startsWith("/")) title = title.substring(1);

            symbols.putIfAbsent(title, new HashSet<>());

            String name = state.title.substring(state.title.lastIndexOf('/') + 1);
            symbols.get(title).add(name);
        }
        for (String symbol : symbols.keySet()) {
            if (symbol.equals("")) continue;
            if (symbols.get("").contains(symbol)) continue;
            // Symbol is not present.
            states.add(getSymbol(symbol));
        }
        states.removeIf(symbol -> !(Set.of("atm", "rec", "ali", "rcp", "con", "var", "per", "par", "fun", "prc", "trp").contains(symbol.type)));
        return states;
    }

    @Override
    public @Nullable SymbolState getSymbol(@NotNull String name) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(path.resolve("/rw/rapid/symbol/properties/RAPID/" + name)).GET().build();
        try {
            CollectionModel collectionModel = send(request);
            assert collectionModel.models().size() == 1;
            Model model = collectionModel.models().get(0);
            return getSymbolState(model);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == 400) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private @NotNull SymbolState getSymbolState(@NotNull Model model) {
        assert model.type().startsWith("rap-symprop");
        SymbolState state = new SymbolState();
        state.title = model.title();
        state.name = model.title().substring(model.title().lastIndexOf('/') + 1);
        state.type = Objects.requireNonNull(model.field("symtyp"));
        state.isLocal = Boolean.parseBoolean(model.field("local"));
        state.isTask = Boolean.parseBoolean(model.field("taskvar"));
        state.dataType = model.field("dattyp");
        state.isRequired = Boolean.parseBoolean(model.field("required"));
        state.mode = model.field("mode");
        for (String value : new String[]{"npar", "ncom"}) {
            String field = model.field(value);
            if (field != null) {
                state.length = Integer.parseInt(field);
            }
        }
        for (String value : new String[]{"parnum", "comnum"}) {
            String field = model.field(value);
            if (field != null) {
                state.index = Integer.parseInt(field) - 1;
            }
        }
        return state;
    }

    @Override
    public void close() throws IOException {
        // Attempt to close any ongoing subscriptions.
    }

    public record CollectionModel(@NotNull Model model, @NotNull List<Model> models) {

        public static @NotNull CollectionModel getModel(@NotNull InputStream inputStream) throws IOException {
            Document document = getDocument(inputStream);
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

        private static @NotNull Document getDocument(@NotNull InputStream inputStream) throws IOException {
            DocumentBuilder builder = getBuilder();
            try {
                return builder.parse(inputStream);
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

    public record Model(@NotNull String type, @NotNull String title, @NotNull List<Link> links,
                        @NotNull Map<String, String> fields) {

        public static @NotNull Model getModel(@NotNull URI path, @NotNull Element element) {
            String title = element.getAttribute("title");
            String type = element.getAttribute("class");
            Map<String, String> fields = getFields(element);
            List<Link> links = getLinks(path, element);
            return new Model(type, title, links, fields);
        }

        public static @NotNull Model getEntityModel(@NotNull URI path, @NotNull Element element) throws IOException {
            String title = getTitle(element);
            String type = getType(element);
            Map<String, String> fields = getFields((Element) element.getElementsByTagName("div").item(0));
            List<Link> links = getLinks(path, (Element) element.getElementsByTagName("div").item(0));
            return new Model(type, title, links, fields);
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

        public @Nullable String field(@NotNull String type) {
            return fields().get(type);
        }

        public @Nullable Link link(@NotNull String type) {
            return links().stream()
                    .filter(link -> link.type().equals(type))
                    .findFirst().orElse(null);
        }

    }

    public record Link(@NotNull String type, @NotNull URI path) {

        public static @NotNull Link getLink(@NotNull URI path, @NotNull Element element) {
            String type = element.getAttribute("rel");
            return new Link(type, path.resolve(element.getAttribute("href")));
        }

    }
}
