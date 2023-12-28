package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

public record BlockDescriptor(@NotNull String moduleName, @NotNull String name) {

    public BlockDescriptor {
        moduleName = moduleName.toLowerCase();
        name = name.toLowerCase();
    }

    public static @NotNull BlockDescriptor getBlockKey(@NotNull Block block) {
        return new BlockDescriptor(block.getModuleName(), block.getName());
    }

    public static @NotNull BlockDescriptor getBlockKey(@NotNull String name) {
        int index = name.indexOf(':');
        if (index < 0) {
            index = 0;
        }
        String moduleName = name.substring(0, index);
        String routineName = name.substring(index + 1);
        return new BlockDescriptor(moduleName, routineName);
    }

}
