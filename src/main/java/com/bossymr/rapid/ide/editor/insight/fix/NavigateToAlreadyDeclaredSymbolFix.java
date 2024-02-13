package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModCommand;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiBasedModCommandAction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class NavigateToAlreadyDeclaredSymbolFix extends PsiBasedModCommandAction<PhysicalSymbol> {

    public NavigateToAlreadyDeclaredSymbolFix(@NotNull PhysicalSymbol element) {
        super(element);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.navigate.to.already.declared.symbol");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalSymbol element) {
        String name = Objects.requireNonNullElseGet(element.getName(), RapidSymbol::getDefaultText);
        return Presentation.of(RapidBundle.message("quick.fix.text.navigate.to.already.declared.symbol", name));
    }

    @Override
    protected @NotNull ModCommand perform(@NotNull ActionContext context, @NotNull PhysicalSymbol element) {
        PsiElement identifier = element.getNameIdentifier();
        return ModCommand.select(identifier != null ? identifier : element);
    }
}
