package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class RenameElementFix extends PsiUpdateModCommandAction<PsiNamedElement> {

    private final String newName;

    public RenameElementFix(@NotNull PsiNamedElement symbol, @NotNull String newName) {
        super(symbol);
        this.newName = newName;
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PsiNamedElement element) {
        String name = element.getName();
        if (name == null || !(RenameUtil.isValidName(context.project(), element, newName)) || name.equals(newName)) {
            return null;
        }
        return Presentation.of(RapidBundle.message("quick.fix.text.rename.element", name, newName));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PsiNamedElement element, @NotNull ModPsiUpdater updater) {
        RenameProcessor processor = new RenameProcessor(context.project(), element, newName, false, false);
        processor.run();
    }
}
