package com.bossymr.rapid.language.symbol.physical;

import com.intellij.model.Pointer;
import com.intellij.platform.backend.navigation.NavigationRequest;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PhysicalNavigationTarget implements NavigationTarget {

    private final @NotNull PhysicalSymbol symbol;

    public PhysicalNavigationTarget(@NotNull PhysicalSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public @NotNull Pointer<PhysicalNavigationTarget> createPointer() {
        return Pointer.delegatingPointer(symbol.createPointer(), PhysicalNavigationTarget::new);
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return symbol.getTargetPresentation();
    }

    @Override
    public @Nullable NavigationRequest navigationRequest() {
        return NavigationRequest.sourceNavigationRequest(symbol.getContainingFile(), symbol.getTextRange());
    }
}
