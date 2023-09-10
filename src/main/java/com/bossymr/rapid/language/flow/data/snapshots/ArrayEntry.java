package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

public sealed interface ArrayEntry {

    @NotNull Expression getValue();

    record Assignment(@NotNull Expression index, @NotNull Expression value) implements ArrayEntry {
        @Override
        public @NotNull Expression getValue() {
            return value;
        }
    }

    record DefaultValue(@NotNull Expression defaultValue) implements ArrayEntry {
        @Override
        public @NotNull Expression getValue() {
            return defaultValue;
        }
    }

}
