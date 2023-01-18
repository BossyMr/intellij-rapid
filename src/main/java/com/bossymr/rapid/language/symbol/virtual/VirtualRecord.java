package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record VirtualRecord(
        @NotNull Visibility visibility,
        @NotNull String name,
        @NotNull List<RapidComponent> components
) implements RapidRecord, VirtualSymbol {

    public VirtualRecord(@NotNull String name, @NotNull List<RapidComponent> components) {
        this(Visibility.GLOBAL, name, components);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return components();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}
