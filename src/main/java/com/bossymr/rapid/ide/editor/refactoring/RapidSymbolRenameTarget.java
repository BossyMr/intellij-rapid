package com.bossymr.rapid.ide.editor.refactoring;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.model.Pointer;
import com.intellij.navigation.TargetPresentation;
import com.intellij.refactoring.rename.api.RenameTarget;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidSymbolRenameTarget implements RenameTarget {

    private final @NotNull PhysicalSymbol symbol;

    public RapidSymbolRenameTarget(@NotNull PhysicalSymbol symbol) {
        this.symbol = symbol;
    }

    public @NotNull PhysicalSymbol getSymbol() {
        return symbol;
    }

    @Override
    public @NotNull String getTargetName() {
        return symbol.getPresentableName();
    }

    @Override
    public @NotNull Pointer<? extends RenameTarget> createPointer() {
        return Pointer.delegatingPointer(symbol.createPointer(), RapidSymbolRenameTarget::new);
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        return symbol.getTargetPresentation();
    }
}
