package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlockDescriptor(@NotNull String moduleName, @NotNull String name) {

    public BlockDescriptor {
        moduleName = moduleName.toLowerCase();
        name = name.toLowerCase();
    }

    public static @NotNull BlockDescriptor getBlockKey(@NotNull Block block) {
        return new BlockDescriptor(Objects.requireNonNullElse(block.getModuleName(), ""), block.getName());
    }

}
