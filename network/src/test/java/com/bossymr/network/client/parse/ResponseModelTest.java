package com.bossymr.network.client.parse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.stream.Collectors;

class ResponseModelTest {

    private String trimString(String text) {
        return text.lines()
                .map(String::trim)
                .collect(Collectors.joining());
    }

    @Test
    void entityToXML() {
        ResponseModel model = ResponseModel.newBuilder("model-type", "model-title")
                .property("property-key", "value")
                .link("link-key", URI.create("http://localhost"))
                .entity("entity-type", "model-title", builder ->
                        builder.property("property-key", "value")
                                .link("link-key", URI.create("http://localhost")))
                .build();
        Assertions.assertEquals(trimString("""
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <title>model-title</title>
                    <base href=""/>
                </head>
                <body>
                <div class="model-type">
                    <a rel="link-key" href="http://localhost"></a>
                    <span class="property-key">value</span>
                    <ul>
                        <li class="entity-type" title="model-title">
                            <a rel="link-key" href="http://localhost"></a>
                            <span class="property-key">value</span>
                        </li>
                    </ul>
                </div>
                </body>
                </html>
                """),model.toXML());
    }

    @Test
    void entityFromXML() {
        ResponseModel model = ResponseModel.newBuilder("model-type", "model-title")
                .property("property-key", "value")
                .link("link-key", URI.create("http://localhost"))
                .entity("entity-type", "model-title", builder ->
                        builder.property("property-key", "value")
                                .link("link-key", URI.create("http://localhost")))
                .build();
        Assertions.assertEquals(model, ResponseModel.fromXML("""
                <?xml version="1.0" encoding="UTF-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <title>model-title</title>
                    <base href=""/>
                </head>
                <body>
                <div class="model-type">
                    <a rel="link-key" href="http://localhost"></a>
                    <span class="property-key">value</span>
                    <ul>
                        <li class="entity-type" title="model-title">
                            <a rel="link-key" href="http://localhost"></a>
                            <span class="property-key">value</span>
                        </li>
                    </ul>
                </div>
                </body>
                </html>
                """));
    }
}
