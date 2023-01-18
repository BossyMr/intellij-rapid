package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

public record VirtualAlias(
        @NotNull Visibility visibility,
        @NotNull String name,
        @NotNull RapidType type
) implements RapidAlias, VirtualSymbol {

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}
