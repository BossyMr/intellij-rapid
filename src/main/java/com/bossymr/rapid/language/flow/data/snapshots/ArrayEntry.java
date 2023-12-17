package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

public sealed interface ArrayEntry {

    @NotNull Snapshot snapshot();

    record Assignment(@NotNull Expression index, @NotNull Snapshot snapshot) implements ArrayEntry {}

    record DefaultValue(@NotNull Snapshot snapshot) implements ArrayEntry {}

}
