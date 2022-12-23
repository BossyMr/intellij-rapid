package com.bossymr.rapid.robot.network;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

public record Link(@NotNull String relationship, @NotNull URI path) {}
