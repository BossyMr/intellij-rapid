package com.bossymr.rapid.robot.network.client.model;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

public record Link(@NotNull String type, @NotNull URI path) {}
