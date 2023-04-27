package com.bossymr.rapid.ide.search;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.lang.Language;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.search.CodeReferenceSearcher;
import com.intellij.model.search.LeafOccurrence;
import com.intellij.model.search.SearchRequest;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class RapidCodeReferenceSearcher implements CodeReferenceSearcher {
    @Override
    public @NotNull Language getReferencingLanguage(@NotNull Symbol target) {
        return RapidLanguage.INSTANCE;
    }

    @Override
    public @Nullable SearchRequest getSearchRequest(@NotNull Project project, @NotNull Symbol target) {
        if (!(target instanceof RapidSymbol symbol)) {
            return null;
        }
        String name = symbol.getName();
        if (name == null) {
            return null;
        }
        if (symbol instanceof PhysicalSymbol physicalSymbol) {
            return SearchRequest.of(name, physicalSymbol.getUseScope());
        }
        return SearchRequest.of(name);
    }

    @Override
    public @NotNull Collection<? extends PsiSymbolReference> getReferences(@NotNull Symbol target, @NotNull LeafOccurrence occurrence) {
        Collection<PsiSymbolReference> references = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(occurrence.getStart(), occurrence.getScope(), (element, previous) -> {
            Collection<? extends PsiSymbolReference> result = element.getOwnReferences();
            references.addAll(result);
            return result.isEmpty();

        });
        return references;
    }
}
