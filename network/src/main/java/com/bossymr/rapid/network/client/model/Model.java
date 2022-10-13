package com.bossymr.rapid.network.client.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Model(@NotNull EntityModel entity, @NotNull List<EntityModel> entities) {}
