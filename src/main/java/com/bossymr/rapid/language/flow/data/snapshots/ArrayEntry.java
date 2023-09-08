package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.value.Value;
import org.jetbrains.annotations.NotNull;

public sealed interface ArrayEntry {

    @NotNull Value getValue();

    record Assignment(@NotNull Value index, @NotNull Value value) implements ArrayEntry {
        @Override
        public @NotNull Value getValue() {
            return value;
        }
    }

    record DefaultValue(@NotNull Value defaultValue) implements ArrayEntry {
        @Override
        public @NotNull Value getValue() {
            return defaultValue;
        }
    }

}
