package com.bossymr.rapid.language.symbol.virtual;

import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VirtualPointer implements Pointer<VirtualSymbol> {

    private final String name;

    public VirtualPointer(@NotNull VirtualSymbol symbol) {
        this.name = symbol.getName();
    }

    @Override
    public @Nullable VirtualSymbol dereference() {
        // TODO: 2022-10-28 Fetch symbol
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualPointer that = (VirtualPointer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
