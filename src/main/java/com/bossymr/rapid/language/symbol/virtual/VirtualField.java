package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VirtualField implements RapidField, VirtualSymbol {

    private final Visibility visibility;
    private final Attribute attribute;
    private final String name;
    private final RapidType type;

    public VirtualField(@NotNull Visibility visibility, @NotNull Attribute attribute, @NotNull String name, @NotNull RapidType type) {
        this.visibility = visibility;
        this.attribute = attribute;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
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
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualField that = (VirtualField) o;
        return getVisibility() == that.getVisibility() && getAttribute() == that.getAttribute() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getAttribute(), getName(), getType());
    }

    @Override
    public String toString() {
        return "VirtualField:" + getName();
    }
}
