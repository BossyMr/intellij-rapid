package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A quick fix which renames the file on which it is applied, to a specified new name.
 */
public class RenameFileFix implements IntentionAction, LocalQuickFix {

    private final String previousName, newName;
    private final SmartPsiElementPointer<PsiFile> pointer;

    /**
     * Creates a new quick fix which will rename the file on which it is applied to the specified name.
     *
     * @param newName the new file name.
     */
    public RenameFileFix(@NotNull PsiFile file, @NotNull String newName) {
        this.previousName = file.getName();
        this.newName = newName;
        this.pointer = SmartPointerManager.createPointer(file);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.rename.file", previousName, newName);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.file");
    }

    @Override
    public @IntentionName @NotNull String getName() {
        return getText();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        WriteCommandAction.writeCommandAction(project).run(() -> invoke(project, null, null));
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        if (isAvailable(project, editor, file)) {
            PsiFile element = pointer.getElement();
            if (element != null) {
                VirtualFile virtualFile = element.getVirtualFile();
                if (virtualFile == null) throw new IncorrectOperationException();
                Document document = PsiDocumentManager.getInstance(project).getDocument(element);
                if (document == null) throw new IncorrectOperationException();
                FileDocumentManager.getInstance().saveDocument(document);
                try {
                    virtualFile.rename(this, newName);
                } catch (IOException e) {
                    Messages.showErrorDialog(project, e.getMessage(), RapidBundle.message("error.cannot.rename.file"));
                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        PsiFile element = pointer.getElement();
        return element != null && element.getContainingDirectory().getVirtualFile().findChild(newName) == null;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
        return IntentionPreviewInfo.rename(previewDescriptor.getPsiElement().getContainingFile(), newName);
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return IntentionPreviewInfo.rename(file, newName);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
