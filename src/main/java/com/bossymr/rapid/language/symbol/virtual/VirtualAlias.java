package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VirtualAlias implements RapidAlias, VirtualStructure {

    private final @NotNull String name;
    private final @NotNull RapidType type;

    public VirtualAlias(@NotNull String name, @NotNull RapidType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return Visibility.GLOBAL;
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
    public @NotNull VirtualPointer<VirtualAlias> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualAlias that = (VirtualAlias) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "VirtualAlias{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
