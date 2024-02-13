package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class MoveModuleToSeparateFileFix extends PsiUpdateModCommandAction<PhysicalModule> {

    public MoveModuleToSeparateFileFix(@NotNull PhysicalModule module) {
        super(module);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.move.module.to.separate.file");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalModule element) {
        String name = element.getName();
        if (name == null) {
            return null;
        }
        PsiDirectory directory = element.getContainingFile().getContainingDirectory();
        PsiFile file = directory.findFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
        if (file != null) {
            return null;
        }
        return Presentation.of(RapidBundle.message("quick.fix.text.move.module.to.separate.file", name));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalModule element, @NotNull ModPsiUpdater updater) {
        String name = Objects.requireNonNull(element.getName());
        PsiDirectory directory = element.getContainingFile().getContainingDirectory();
        PsiFile file = directory.createFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
        file.add(element);
        element.delete();
        updater.moveTo(file);
    }

    @Override
    protected @NotNull IntentionPreviewInfo generatePreview(@NotNull ActionContext context, @NotNull PhysicalModule element) {
        return IntentionPreviewInfo.movePsi(element, element.getContainingFile().getContainingDirectory());
    }
}