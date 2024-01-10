package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RenameElementFix extends RapidQuickFix {

    private final String newName;

    /**
     * Creates a new quick which will rename the specified symbol to the specified name.
     *
     * @param symbol the symbol to rename.
     * @param newName the new symbol name.
     */
    public RenameElementFix(@Nullable PsiElement symbol, @NotNull String newName) {
        super(symbol);
        this.newName = newName;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        PhysicalSymbol element = (PhysicalSymbol) getStartElement();
        String name = element != null ? element.getName() : null;
        return RapidBundle.message("quick.fix.text.rename.element", name != null ? name : "", newName);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        RefactoringFactory refactoringFactory = RefactoringFactory.getInstance(project);
        RenameRefactoring refactoring = refactoringFactory.createRename(startElement, newName);
        refactoring.run();
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(getStartElement() instanceof PsiFile element)) {
            return super.generatePreview(project, editor, file);
        }
        return IntentionPreviewInfo.rename(element, newName);
    }
}
