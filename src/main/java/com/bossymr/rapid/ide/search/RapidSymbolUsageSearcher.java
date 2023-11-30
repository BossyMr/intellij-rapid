package com.bossymr.rapid.ide.search;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.find.usages.api.PsiUsage;
import com.intellij.find.usages.api.Usage;
import com.intellij.find.usages.api.UsageSearchParameters;
import com.intellij.find.usages.api.UsageSearcher;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.search.LeafOccurrence;
import com.intellij.model.search.LeafOccurrenceMapper;
import com.intellij.model.search.SearchContext;
import com.intellij.model.search.SearchService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidSymbolUsageSearcher implements UsageSearcher {

    public static @NotNull Query<? extends PsiUsage> collectSearchRequests(@NotNull RapidSymbol symbol, @NotNull String name, @NotNull Project project, @NotNull SearchScope searchScope) {
        return SearchService.getInstance()
                .searchWord(project, name)
                .caseSensitive(false)
                .inContexts(SearchContext.IN_CODE, SearchContext.IN_COMMENTS, SearchContext.IN_STRINGS)
                .inScope(searchScope)
                .buildQuery(LeafOccurrenceMapper.withPointer(symbol.createPointer(), RapidSymbolUsageSearcher::collectReferences))
                .mapping(PsiUsage::textUsage);
    }

    @Contract(pure = true)
    private static @NotNull Collection<? extends PsiSymbolReference> collectReferences(@NotNull RapidSymbol symbol, @NotNull LeafOccurrence occurrence) {
        List<PsiSymbolReference> references = new ArrayList<>();
        PsiTreeUtil.treeWalkUp(occurrence.getStart(), occurrence.getScope(), (element, previous) -> {
            references.addAll(element.getOwnReferences());
            return references.isEmpty();
        });
        return references;
    }

    @Override
    public @NotNull Collection<? extends Query<? extends Usage>> collectSearchRequests(@NotNull UsageSearchParameters parameters) {
        if (!(parameters.getTarget() instanceof RapidSymbolSearchTarget target)) {
            return List.of();
        }
        RapidSymbol symbol = target.getSymbol();
        String name = symbol.getName();
        if (name == null) {
            return List.of();
        }
        return List.of(collectSearchRequests(symbol, name, parameters.getProject(), parameters.getSearchScope()));
    }

}
