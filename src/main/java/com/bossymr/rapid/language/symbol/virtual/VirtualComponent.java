package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class VirtualComponent implements RapidComponent, VirtualSymbol {

    private final RapidType type;
    private final String name;

    public VirtualComponent(@NotNull String name, @NotNull RapidType type) {
        this.type = type;
        this.name = name;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualComponent that = (VirtualComponent) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName());
    }

    @Override
    public String toString() {
        return "VirtualComponent:" + getName();
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
