package com.bossymr.rapid.language.symbol;

import com.intellij.model.Pointer;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidPointer} is used to restore a {@link RapidSymbol} between read-actions; as a write-action might modify
 * the symbol.
 *
 * @param <T> the type of the underlying element.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidPointer<T extends RapidSymbol> extends Pointer<T> {

    /**
     * Attempts to restore the symbol. If the symbol is not found, or if the symbol is of a different type, it is
     * considered invalidated.
     *
     * @return the symbol, or {@code null} if the symbol is invalidated.
     */
    @Override
    @Nullable T dereference();
}
