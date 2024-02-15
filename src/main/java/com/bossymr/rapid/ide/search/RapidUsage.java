package com.bossymr.rapid.ide.search;

import com.intellij.find.usages.api.PsiUsage;
import com.intellij.model.Pointer;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidUsage implements PsiUsage {

    private final boolean isDeclaration;
    private final @NotNull PsiFile file;
    private final @NotNull TextRange range;

    public RapidUsage(@NotNull PsiSymbolReference reference) {
        this(false, reference.getElement().getContainingFile(), reference.getAbsoluteRange());
    }

    public RapidUsage(@NotNull PsiSymbolDeclaration declaration) {
        this(true, declaration.getDeclaringElement().getContainingFile(), declaration.getAbsoluteRange());
    }

    private RapidUsage(boolean isDeclaration, @NotNull PsiFile file, @NotNull TextRange absoluteRange) {
        this.file = file;
        this.range = absoluteRange;
        this.isDeclaration = isDeclaration;
    }

    @Override
    public @NotNull PsiFile getFile() {
        return file;
    }

    @Override
    public @NotNull TextRange getRange() {
        return range;
    }

    @Override
    public @NotNull Pointer<? extends PsiUsage> createPointer() {
        return Pointer.fileRangePointer(file, range, (file, range) -> new RapidUsage(isDeclaration, file, range));
    }

    @Override
    public boolean getDeclaration() {
        return isDeclaration;
    }
}
