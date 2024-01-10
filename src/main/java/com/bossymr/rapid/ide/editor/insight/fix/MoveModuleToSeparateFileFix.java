package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MoveModuleToSeparateFileFix extends RapidQuickFix {

    public MoveModuleToSeparateFileFix(@NotNull PhysicalModule module) {
        super(module);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        PhysicalModule element = ((PhysicalModule) getStartElement());
        String name = element != null ? element.getName() : null;
        return RapidBundle.message("quick.fix.text.move.module.to.separate.file", name != null ? name : "");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.move.module.to.separate.file");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        PhysicalModule module = (PhysicalModule) startElement;
        String name = module.getName();
        if (name == null) {
            return;
        }
        PsiDirectory directory = module.getContainingFile().getContainingDirectory();
        PsiFile newFile = directory.createFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
        PsiElement copy = module.copy();
        newFile.add(copy);
        module.delete();
        Navigatable navigatable = PsiNavigationSupport.getInstance().createNavigatable(project, newFile.getVirtualFile(), copy.getTextOffset());
        navigatable.navigate(true);
    }
}