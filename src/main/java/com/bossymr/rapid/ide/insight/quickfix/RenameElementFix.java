package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A quick fix which renames a specified element to a specified new name.
 */
public class RenameElementFix implements IntentionAction, LocalQuickFix {

    private final String previousName, newName;
    private final SmartPsiElementPointer<PhysicalSymbol> pointer;

    /**
     * Creates a new quick which will rename the specified symbol to the specified name.
     *
     * @param element the element to rename.
     * @param newName the new element name.
     */
    public RenameElementFix(@NotNull PhysicalSymbol element, @NotNull String newName) {
        this.previousName = element.getName();
        assert previousName != null;
        this.newName = newName;
        this.pointer = SmartPointerManager.createPointer(element);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.rename.element", previousName, newName);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    public @IntentionName @NotNull String getName() {
        return getText();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        invoke(project, null, null);
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        if (isAvailable(project, editor, file)) {
            RefactoringFactory refactoringFactory = RefactoringFactory.getInstance(project);
            PsiElement element = pointer.getElement();
            if (element != null) {
                RenameRefactoring refactoring = refactoringFactory.createRename(element, newName);
                refactoring.run();
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        PsiElement element = pointer.getElement();
        return element != null;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
