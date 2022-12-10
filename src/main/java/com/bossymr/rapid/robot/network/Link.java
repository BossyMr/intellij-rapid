package com.bossymr.rapid.robot.network;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.net.URI;

public record Link(@NotNull String relationship, @NotNull URI path) {

    public static @NotNull Link getLink(@NotNull URI path, @NotNull Element element) {
        String type = element.getAttribute("rel");
        return new Link(type, path.resolve(element.getAttribute("href")));
    }

}
