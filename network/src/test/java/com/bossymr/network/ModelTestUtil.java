package com.bossymr.network;

import com.bossymr.network.model.CollectionModel;
import com.bossymr.network.model.Model;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class ModelTestUtil {

    public static @NotNull ResponseDefinitionBuilder response(@NotNull URI defaultPath, @NotNull CollectionModel collectionModel) {
        return WireMock.okForContentType("application/x-www-form-urlencoded", toString(defaultPath, collectionModel));
    }

    public static @NotNull String toString(@NotNull URI defaultPath, @NotNull CollectionModel collectionModel) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        stringBuilder.append("<html>");
        stringBuilder.append("<head>")
                .append("<title>").append(collectionModel.getTitle()).append("</title>")
                .append("<base href=\"").append(defaultPath).append("\"/>")
                .append("</head>");
        stringBuilder.append("<body>");
        stringBuilder.append("<div class=\"").append(collectionModel.getType()).append("\">");
        appendEntity(stringBuilder, collectionModel);
        stringBuilder.append("<ul>");
        for (Model model : collectionModel.getModels()) {
            stringBuilder.append("<li class=\"").append(model.getType()).append("\" title=\"").append(model.getTitle()).append("\">");
            appendEntity(stringBuilder, model);
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        stringBuilder.append("</div>");
        stringBuilder.append("</body>");
        stringBuilder.append("</html>");
        return stringBuilder.toString();

    }

    private static void appendEntity(@NotNull StringBuilder stringBuilder, @NotNull Model model) {
        model.getFields().forEach((type, value) -> {
            stringBuilder.append("<span class=\"").append(type).append("\">");
            stringBuilder.append(value).append("</span>");
        });
        model.getLinks().forEach((type, link) -> {
            stringBuilder.append("<a href=\"").append(link).append("\" rel=\"").append(type).append("\"> </a>");
        });
    }
}
