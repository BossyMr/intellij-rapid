package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A quick fix which moves a module to a separate file.
 */
public final class MoveModuleToSeparateFileFix implements IntentionAction {

    private final PhysicalModule module;

    public MoveModuleToSeparateFileFix(@NotNull PhysicalModule module) {
        this.module = module;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.move.module.to.separate.file", module.getName());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.move.module.to.separate.file");
    }

    @Override
    public @NotNull FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return new MoveModuleToSeparateFileFix(PsiTreeUtil.findSameElementInCopy(module, target));
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        PsiDirectory directory = module.getContainingFile().getContainingDirectory();
        WriteAction.run(() -> {
            PsiFile newFile = directory.createFile(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION);
            PsiElement copy = module.copy();
            newFile.add(copy);
            module.delete();

            Navigatable navigatable = PsiNavigationSupport.getInstance().createNavigatable(project, newFile.getVirtualFile(), module.getTextOffset());
            navigatable.navigate(true);
        });
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        if (!(module.isValid()) || !(BaseIntentionAction.canModify(module))) return false;
        PsiDirectory directory = module.getContainingFile().getContainingDirectory();
        try {
            directory.checkCreateFile(module.getName() + RapidFileType.DEFAULT_DOT_EXTENSION);
            return true;
        } catch (IncorrectOperationException e) {
            return false;
        }
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return IntentionPreviewInfo.movePsi(module, module.getContainingFile().getContainingDirectory());
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}