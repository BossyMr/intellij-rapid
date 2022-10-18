package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidField;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightField extends LightSymbol implements RapidField {

    private final Attribute attribute;
    private final RapidType type;
    private final String name;

    public LightField(@NotNull Attribute attribute, @NotNull RapidType type, @NotNull String name) {
        this.attribute = attribute;
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isTask() {
        return false;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
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
    public @Nullable RapidType getType() {
        return type;
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
