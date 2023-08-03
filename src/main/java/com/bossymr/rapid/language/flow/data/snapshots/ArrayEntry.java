package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

public sealed interface ArrayEntry {

    record Assignment(@NotNull Value index, @NotNull Value value) implements ArrayEntry {}

    record DefaultValue(@NotNull Value defaultValue) implements ArrayEntry {}

}
