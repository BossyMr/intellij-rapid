package com.bossymr.rapid.ide.search;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.find.usages.api.PsiUsage;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage;
import com.intellij.refactoring.rename.api.PsiRenameUsage;
import com.intellij.refactoring.rename.api.RenameUsageFileUpdaters;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidSymbolModifiableRenameUsage implements PsiRenameUsage, PsiModifiableRenameUsage {

    private final @NotNull PhysicalSymbol symbol;
    private final @NotNull PsiRenameUsage delegate;

    public RapidSymbolModifiableRenameUsage(@NotNull PhysicalSymbol symbol, @NotNull PsiUsage usage) {
        this.symbol = symbol;
        this.delegate = PsiRenameUsage.defaultPsiRenameUsage(usage);
    }

    private RapidSymbolModifiableRenameUsage(@NotNull PhysicalSymbol symbol, @NotNull PsiRenameUsage delegate) {
        this.symbol = symbol;
        this.delegate = delegate;
    }

    @Override
    public @NotNull PsiFile getFile() {
        return delegate.getFile();
    }

    @Override
    public @NotNull FileUpdater getFileUpdater() {
        return RenameUsageFileUpdaters.fileRangeUpdater((newName) -> {
            symbol.setName(newName);
            return newName;
        });
    }

    @Override
    public @NotNull TextRange getRange() {
        return delegate.getRange();
    }

    @Override
    public @NotNull Pointer<? extends PsiModifiableRenameUsage> createPointer() {
        Pointer<? extends PhysicalSymbol> symbolPointer = symbol.createPointer();
        Pointer<? extends PsiRenameUsage> delegatePointer = delegate.createPointer();
        return (Pointer<PsiModifiableRenameUsage>) () -> {
            PhysicalSymbol symbol = symbolPointer.dereference();
            PsiRenameUsage delegate = delegatePointer.dereference();
            if (symbol == null || delegate == null) {
                return null;
            }
            return new RapidSymbolModifiableRenameUsage(symbol, delegate);
        };
    }

    @Override
    public boolean getDeclaration() {
        return delegate.getDeclaration();
    }
}
