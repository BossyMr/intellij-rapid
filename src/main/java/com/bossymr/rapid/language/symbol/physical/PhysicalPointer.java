package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.symbol.RapidPointer;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code PhysicalPointer} attempts to restore a {@link PhysicalSymbol}.
 *
 * @param <T> the type of the underlying element.
 */
@SuppressWarnings("UnstableApiUsage")
public class PhysicalPointer<T extends PhysicalSymbol> implements RapidPointer<T> {

    private final @NotNull SmartPsiElementPointer<T> pointer;

    public PhysicalPointer(@NotNull T symbol) {
        this.pointer = SmartPointerManager.createPointer(symbol);
    }

    @Override
    public @Nullable T dereference() {
        return pointer.dereference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalPointer<?> that = (PhysicalPointer<?>) o;
        return Objects.equals(pointer, that.pointer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }

    @Override
    public String toString() {
        return "PhysicalPointer{" +
                "pointer=" + pointer +
                '}';
    }
}
