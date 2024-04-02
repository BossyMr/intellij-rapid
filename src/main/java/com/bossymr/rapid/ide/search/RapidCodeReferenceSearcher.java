package com.bossymr.rapid.ide.search;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.lang.Language;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.psi.PsiSymbolReferenceService;
import com.intellij.model.search.CodeReferenceSearcher;
import com.intellij.model.search.LeafOccurrence;
import com.intellij.model.search.SearchRequest;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidCodeReferenceSearcher implements CodeReferenceSearcher {

    @Override
    public @NotNull Language getReferencingLanguage(@NotNull Symbol target) {
        return RapidLanguage.getInstance();
    }

    @Override
    public @Nullable SearchRequest getSearchRequest(@NotNull Project project, @NotNull Symbol target) {
        if(target instanceof VirtualSymbol virtualSymbol) {
            return SearchRequest.of(virtualSymbol.getName());
        }
        if(target instanceof PhysicalSymbol physicalSymbol) {
            String name = physicalSymbol.getName();
            if(name == null) {
                return null;
            }
            SearchScope useScope = physicalSymbol.getUseScope();
            return SearchRequest.of(name, useScope);
        }
        return null;
    }

    @Override
    public @NotNull Collection<? extends @NotNull PsiSymbolReference> getReferences(@NotNull Symbol target, @NotNull LeafOccurrence occurrence) {
        List<PsiSymbolReference> references = new ArrayList<>();
        PsiSymbolReferenceService service = PsiSymbolReferenceService.getService();
        PsiTreeUtil.treeWalkUp(occurrence.getStart(), occurrence.getScope(), (element, previous) -> {
            for (PsiSymbolReference reference : service.getReferences(element)) {
                if (reference.resolvesTo(target)) {
                    references.add(reference);
                }
            }
            return references.isEmpty();
        });
        return references;
    }
}
