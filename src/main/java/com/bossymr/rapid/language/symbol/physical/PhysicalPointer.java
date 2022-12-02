package com.bossymr.rapid.language.symbol.physical;

import com.intellij.model.Pointer;
import com.intellij.psi.SmartPointerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalPointer implements Pointer<PhysicalSymbol> {

    private final Pointer<PhysicalSymbol> pointer;

    public PhysicalPointer(@NotNull PhysicalSymbol symbol) {
        this.pointer = SmartPointerManager.createPointer(symbol);
    }

    @Override
    public @Nullable PhysicalSymbol dereference() {
        return pointer.dereference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalPointer that = (PhysicalPointer) o;
        return Objects.equals(pointer, that.pointer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }
}
