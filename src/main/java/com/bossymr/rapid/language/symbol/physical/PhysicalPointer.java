package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.symbol.RapidPointer;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
