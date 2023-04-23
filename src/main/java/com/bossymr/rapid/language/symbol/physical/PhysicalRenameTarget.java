package com.bossymr.rapid.language.symbol.physical;

import com.intellij.model.Pointer;
import com.intellij.navigation.TargetPresentation;
import com.intellij.refactoring.rename.api.RenameTarget;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class PhysicalRenameTarget implements RenameTarget {

    private final @NotNull PhysicalSymbol symbol;

    public PhysicalRenameTarget(@NotNull PhysicalSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public @NotNull String getTargetName() {
        return symbol.getPresentableName();
    }

    @Override
    public @NotNull Pointer<? extends RenameTarget> createPointer() {
        return Pointer.delegatingPointer(symbol.createPointer(), PhysicalRenameTarget::new);
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        return symbol.getTargetPresentation();
    }
}
