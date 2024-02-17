package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RenameElementFix extends LocalQuickFixAndIntentionActionOnPsiElement {

    private final @NotNull String newName;
    private final @NotNull String text;

    public RenameElementFix(@NotNull PsiNamedElement symbol, @NotNull String newName) {
        super(symbol);
        this.newName = newName;
        String name = Objects.requireNonNullElse(symbol.getName(), "<unknown>");
        this.text = RapidBundle.message("quick.fix.text.rename.element", name, newName);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return text;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if (FileModificationService.getInstance().prepareFileForWrite(file)) {
            RenameProcessor processor = new RenameProcessor(project, startElement, newName, false, false);
            processor.run();
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if (!(startElement instanceof PsiNamedElement symbol)) {
            return false;
        }
        String name = symbol.getName();
        if (name == null) {
            return false;
        }
        if (name.equals(newName)) {
            return false;
        }
        return RenameUtil.isValidName(project, startElement, newName);
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PsiElement element = PsiTreeUtil.findSameElementInCopy(getStartElement(), file);
        if (!(element instanceof PsiNamedElement symbol)) {
            return IntentionPreviewInfo.EMPTY;
        }
        symbol.setName(newName);
        return IntentionPreviewInfo.DIFF;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
