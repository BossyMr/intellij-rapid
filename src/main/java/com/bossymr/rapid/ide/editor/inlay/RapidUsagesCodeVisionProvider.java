package com.bossymr.rapid.ide.editor.inlay;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.inspection.UnusedDeclarationInspection;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalVisibleSymbol;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.hints.codeVision.ReferencesCodeVisionProvider;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.search.SearchService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidUsagesCodeVisionProvider extends ReferencesCodeVisionProvider {

    @Override
    public boolean acceptsElement(@NotNull PsiElement element) {
        if (!(element instanceof PhysicalVisibleSymbol)) {
            return false;
        }
        PsiElement parent = element.getParent();
        return parent != null && PhysicalRoutine.getRoutine(parent) == null;
    }

    @Override
    public boolean acceptsFile(@NotNull PsiFile file) {
        return file.getLanguage() == RapidLanguage.getInstance();
    }

    @Override
    public @Nls @Nullable String getHint(@NotNull PsiElement element, @NotNull PsiFile file) {
        CodeVisionInfo visionInfo = getVisionInfo(element, file);
        if (visionInfo == null) {
            return null;
        }
        return visionInfo.getText();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable CodeVisionInfo getVisionInfo(@NotNull PsiElement element, @NotNull PsiFile file) {
        if (!(element instanceof PhysicalVisibleSymbol symbol)) {
            return null;
        }
        if (UnusedDeclarationInspection.isEntryPoint(symbol)) {
            return null;
        }
        Query<PsiSymbolReference> query = SearchService.getInstance().searchPsiSymbolReferences(symbol.getProject(), symbol, symbol.getUseScope());
        int usageCount = query.findAll().size();
        return new CodeVisionInfo(RapidBundle.message("inlay.usages.text", usageCount), usageCount, true);
    }

    @Override
    public @NotNull String getId() {
        return "rapid.usages";
    }

    @Override
    public @NotNull List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return List.of(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingFirst.INSTANCE);
    }
}
