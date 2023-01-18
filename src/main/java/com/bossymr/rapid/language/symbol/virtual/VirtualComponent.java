package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

public record VirtualComponent(
        @NotNull String name,
        @NotNull RapidType type
) implements RapidComponent, VirtualSymbol {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}
