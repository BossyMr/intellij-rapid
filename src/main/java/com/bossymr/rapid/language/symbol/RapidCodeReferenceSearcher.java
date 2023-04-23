package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.lang.Language;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.search.CodeReferenceSearcher;
import com.intellij.model.search.LeafOccurrence;
import com.intellij.model.search.SearchRequest;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class RapidCodeReferenceSearcher implements CodeReferenceSearcher {
    @Override
    public @NotNull Language getReferencingLanguage(@NotNull Symbol target) {
        return RapidLanguage.INSTANCE;
    }

    @Override
    public @Nullable SearchRequest getSearchRequest(@NotNull Project project, @NotNull Symbol target) {
        if (!(target instanceof RapidSymbol symbol) || symbol.getName() == null) {
            return null;
        }
        return SearchRequest.of(symbol.getName());
    }

    @Override
    public @NotNull Collection<? extends PsiSymbolReference> getReferences(@NotNull Symbol target, @NotNull LeafOccurrence occurrence) {
        PsiElement element = occurrence.getStart();
        return element.getOwnReferences();
    }
}
