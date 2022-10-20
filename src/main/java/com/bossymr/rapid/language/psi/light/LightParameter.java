package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightParameter extends LightSymbol implements RapidParameter {

    private final Attribute attribute;
    private final RapidType type;
    private final String name;

    public LightParameter(@NotNull PsiManager manager, @NotNull Attribute attribute, @NotNull RapidType type, @NotNull String name) {
        super(manager);
        this.attribute = attribute;
        this.type = type;
        this.name = name;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    @Override
    public @NotNull RapidType getType() {
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

    @Override
    public String toString() {
        return "RapidParameter";
    }
}
