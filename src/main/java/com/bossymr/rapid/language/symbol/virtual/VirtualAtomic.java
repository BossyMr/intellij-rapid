package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidAtomic;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VirtualAtomic implements RapidAtomic, VirtualStructure {

    private final @NotNull String name;
    private final @Nullable RapidType type;

    public VirtualAtomic(@NotNull String name, @Nullable RapidType type) {
        this.name = name;
        this.type = type;
    }

    public VirtualAtomic(@NotNull String name) {
        this(name, null);
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
    public @Nullable RapidType getAssociatedType() {
        return type;
    }

    @Override
    public @NotNull VirtualPointer<VirtualAtomic> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualAtomic that = (VirtualAtomic) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "VirtualAtomic{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
