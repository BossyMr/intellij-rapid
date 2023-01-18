package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VirtualField(
        @NotNull Visibility visibility,
        @NotNull Attribute attribute,
        @NotNull String name,
        @NotNull RapidType type,
        boolean readOnly
) implements RapidField, VirtualSymbol {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute();
    }

    @Override
    public @Nullable RapidExpression getInitializer() {
        return null;
    }

    @Override
    public boolean hasInitializer() {
        return false;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}
