package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

public sealed interface ArgumentDescriptor {

    record Optional(@NotNull String name) implements ArgumentDescriptor {}

    record Required(int index) implements ArgumentDescriptor {}

}
