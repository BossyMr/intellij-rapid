package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

public record BlockDescriptor(@NotNull String moduleName, @NotNull String name) {

    public BlockDescriptor {
        moduleName = moduleName.toLowerCase();
        name = name.toLowerCase();
    }
}
