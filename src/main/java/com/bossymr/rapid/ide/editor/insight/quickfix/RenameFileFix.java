package com.bossymr.rapid.ide.editor.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A quick fix which renames the file on which it is applied, to a specified new name.
 */
public class RenameFileFix implements IntentionAction {

    private final String newName;

    public RenameFileFix(@NotNull String newName) {
        this.newName = newName;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.rename.file");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.file");
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        if (file != null) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) throw new IncorrectOperationException();
            Document document = file.getViewProvider().getDocument();
            if (document == null) throw new IncorrectOperationException();
            FileDocumentManager.getInstance().saveDocument(document);
            try {
                virtualFile.rename(this, newName);
            } catch (IOException e) {
                Messages.showErrorDialog(project, e.getMessage(), RapidBundle.message("error.cannot.rename.file"));
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        return file != null && file.isValid() && file.getContainingDirectory().getVirtualFile().findChild(newName) == null;
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
