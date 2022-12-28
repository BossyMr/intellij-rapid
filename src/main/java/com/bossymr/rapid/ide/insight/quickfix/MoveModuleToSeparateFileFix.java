package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A quick fix which moves a module to a separate file.
 */
public class MoveModuleToSeparateFileFix implements IntentionAction, LocalQuickFix {

    private final String name;
    private final SmartPsiElementPointer<PhysicalModule> pointer;

    public MoveModuleToSeparateFileFix(@NotNull PhysicalModule module) {
        this.name = module.getName();
        this.pointer = SmartPointerManager.createPointer(module);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.move.module.to.separate.file", name);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.move.module.to.separate.file");
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
            PhysicalModule module = pointer.getElement();
            if (module != null) {
                PsiDirectory directory = module.getContainingFile().getContainingDirectory();
                if (directory == null) throw new IncorrectOperationException();
                WriteAction.run(() -> {
                    PsiFile newFile = directory.createFile(getFileName());
                    PsiElement copy = module.copy();
                    newFile.add(copy);
                    module.delete();

                    Navigatable navigatable = PsiNavigationSupport.getInstance().createNavigatable(project, newFile.getVirtualFile(), module.getTextOffset());
                    navigatable.navigate(true);
                });
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        PhysicalModule element = pointer.getElement();
        if (element == null) return false;
        if (!(element.isValid()) || !(BaseIntentionAction.canModify(element))) return false;
        PsiDirectory directory = element.getContainingFile().getContainingDirectory();
        if (directory == null) return false;
        try {
            directory.checkCreateFile(getFileName());
            return true;
        } catch (IncorrectOperationException e) {
            return false;
        }
    }

    private @NotNull String getFileName() {
        return name + RapidFileType.DEFAULT_DOT_EXTENSION;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}