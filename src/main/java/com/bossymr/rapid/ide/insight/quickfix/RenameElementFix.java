package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A quick fix which renames a specified element to a specified new name.
 */
public final class RenameElementFix implements IntentionAction {

    private final String newName;
    private final PhysicalSymbol symbol;

    /**
     * Creates a new quick which will rename the specified symbol to the specified name.
     *
     * @param symbol the symbol to rename.
     * @param newName the new symbol name.
     */
    public RenameElementFix(@NotNull PhysicalSymbol symbol, @NotNull String newName) {
        this.newName = newName;
        this.symbol = symbol;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.rename.element", symbol.getName(), newName);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        RefactoringFactory refactoringFactory = RefactoringFactory.getInstance(project);
        RenameRefactoring refactoring = refactoringFactory.createRename(symbol, newName);
        refactoring.run();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return symbol.isValid() && BaseIntentionAction.canModify(symbol);
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhysicalSymbol element = PsiTreeUtil.findSameElementInCopy(symbol, file);
        element.setName(newName);
        return IntentionPreviewInfo.DIFF;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
