package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidAtomic;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VirtualAtomic(
        @NotNull Visibility visibility,
        @NotNull String name,
        @Nullable RapidType type
) implements RapidAtomic, VirtualSymbol {

    public VirtualAtomic(@NotNull String name) {
        this(Visibility.GLOBAL, name, null);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @Nullable RapidType getType() {
        return type();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}
