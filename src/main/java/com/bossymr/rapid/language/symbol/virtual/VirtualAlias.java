package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VirtualAlias implements RapidAlias, VirtualSymbol {

    private final Visibility visibility;
    private final String name;
    private final RapidType type;

    public VirtualAlias(@NotNull Visibility visibility, @NotNull String name, @NotNull RapidType type) {
        this.visibility = visibility;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
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
        VirtualAlias that = (VirtualAlias) o;
        return getVisibility() == that.getVisibility() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getName(), getType());
    }

    @Override
    public String toString() {
        return "VirtualAlias:" + getName();
    }
}
