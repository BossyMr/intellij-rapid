package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class InvokeRenameElementFix extends PsiBasedModCommandAction<PhysicalSymbol> {

    public InvokeRenameElementFix(@NotNull PhysicalSymbol element) {
        super(element);
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalSymbol element) {
        PsiElement identifier = element.getNameIdentifier();
        if (identifier == null) {
            return null;
        }
        return super.getPresentation(context, element);
    }

    @Override
    protected @NotNull ModCommand perform(@NotNull ActionContext context, @NotNull PhysicalSymbol element) {
        VirtualFile file = element.getContainingFile().getViewProvider().getVirtualFile();
        PsiElement identifier = Objects.requireNonNull(element.getNameIdentifier());
        ModStartRename.RenameSymbolRange range = new ModStartRename.RenameSymbolRange(element.getTextRange(), identifier.getTextRange());
        return new ModStartRename(file, range, List.of());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }
}
