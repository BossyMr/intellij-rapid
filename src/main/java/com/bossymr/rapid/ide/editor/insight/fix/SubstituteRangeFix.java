package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubstituteRangeFix extends RapidQuickFix {

    @SafeFieldForPreview
    private final @NotNull SmartPsiFileRange fileWithRange;
    private final @NotNull @IntentionName String intentionName;
    private final @Nullable String substitution;

    private SubstituteRangeFix(@NotNull PsiFile file, @NotNull @IntentionName String intentionName, @NotNull TextRange textRange, @Nullable String substitution) {
        super(file.findElementAt(textRange.getStartOffset()));
        SmartPointerManager manager = SmartPointerManager.getInstance(file.getProject());
        this.fileWithRange = manager.createSmartPsiFileRangePointer(file, textRange);
        this.intentionName = intentionName;
        this.substitution = substitution;
    }

    public static @NotNull SubstituteRangeFix delete(@NotNull @IntentionName String intentionName, @NotNull PsiFile file, @NotNull TextRange textRange) {
        return new SubstituteRangeFix(file, intentionName, textRange, null);
    }

    public static @NotNull SubstituteRangeFix insert(@NotNull @IntentionName String intentionName, @NotNull PsiFile file, int offset, @NotNull String text) {
        return new SubstituteRangeFix(file, intentionName, TextRange.create(offset, offset), text);
    }

    public static @NotNull SubstituteRangeFix modify(@NotNull @IntentionName String intentionName, @NotNull PsiFile file, @NotNull TextRange textRange, @NotNull String text) {
        return new SubstituteRangeFix(file, intentionName, textRange, text);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return intentionName;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.substitute.text");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        Segment range = fileWithRange.getRange();
        if (range == null) {
            return;
        }
        PsiFile containingFile = fileWithRange.getContainingFile();
        if (containingFile == null) {
            return;
        }
        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        Document document = manager.getDocument(containingFile);
        if (document == null) {
            return;
        }
        if (substitution != null) {
            document.replaceString(range.getStartOffset(), range.getEndOffset(), substitution);
        } else {
            document.deleteString(range.getStartOffset(), range.getEndOffset());
        }
    }
}
