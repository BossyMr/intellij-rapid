package com.bossymr.rapid.language.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An {@code ArgumentDescriptor} which describes a specific argument.
 */
public sealed interface ArgumentDescriptor {

    record Optional(@NotNull String name) implements ArgumentDescriptor {}

    record Conditional(@NotNull String name) implements ArgumentDescriptor {}

    record Required(int index) implements ArgumentDescriptor {}

}
