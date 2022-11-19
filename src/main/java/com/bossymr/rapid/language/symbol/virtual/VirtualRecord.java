package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class VirtualRecord implements RapidRecord, VirtualSymbol {

    private final Visibility visibility;
    private final String name;
    private final List<RapidComponent> components;

    public VirtualRecord(@NotNull String name, @NotNull List<RapidComponent> components) {
        this(Visibility.GLOBAL, name, components);
    }

    public VirtualRecord(@NotNull Visibility visibility, @NotNull String name, @NotNull List<RapidComponent> components) {
        this.visibility = visibility;
        this.name = name;
        this.components = components;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return components;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualRecord that = (VirtualRecord) o;
        return getVisibility() == that.getVisibility() && Objects.equals(getName(), that.getName()) && Objects.equals(getComponents(), that.getComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getName(), getComponents());
    }

    @Override
    public String toString() {
        return "VirtualRecord:" + getName();
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
