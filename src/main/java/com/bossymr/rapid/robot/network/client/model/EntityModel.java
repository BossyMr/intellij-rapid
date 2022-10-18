package com.bossymr.rapid.robot.network.client.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record EntityModel(@NotNull String title, @NotNull String type, @NotNull List<Link> links, @NotNull List<Property> properties) {

    public @NotNull Optional<Link> getLink(@NotNull String type) {
        return links.stream()
                .filter(link -> link.type().equals(type))
                .findFirst();
    }

    public @NotNull Optional<Property> getProperty(@NotNull String type) {
        return properties.stream()
                .filter(property -> property.type().equals(type))
                .findFirst();
    }
}
