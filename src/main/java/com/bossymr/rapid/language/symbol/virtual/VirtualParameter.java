package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class VirtualParameter implements RapidParameter, VirtualSymbol {

    private final Attribute attribute;
    private final String name;
    private final RapidType type;

    public VirtualParameter(@NotNull Attribute attribute, @NotNull String name, @NotNull RapidType type) {
        this.attribute = attribute;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualParameter that = (VirtualParameter) o;
        return getAttribute() == that.getAttribute() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttribute(), getName(), getType());
    }

    @Override
    public String toString() {
        return "VirtualParameter:" + getName();
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return new ColoredItemPresentation() {
            @Override
            public @Nullable TextAttributesKey getTextAttributesKey() {
                return null;
            }

            @Override
            public @Nullable String getPresentableText() {
                return getName();
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return null;
            }
        };
    }
}
