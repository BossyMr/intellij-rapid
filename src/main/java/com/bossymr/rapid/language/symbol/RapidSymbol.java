package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * A {@code RapidSymbol} is a symbol.
 */
public interface RapidSymbol {

    /**
     * Returns the name of this symbol.
     *
     * @return the name of this symbol, or {@code null} if this symbol is not complete.
     */
    @Nullable String getName();

    /**
     * Returns the icon of this symbol.
     *
     * @return the icon of this symbol.
     */
    @NotNull Icon getIcon();
}
