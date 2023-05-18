package com.bossymr.rapid.ide.editor.refactoring;

import com.bossymr.rapid.ide.search.RapidSymbolModifiableRenameUsage;
import com.bossymr.rapid.ide.search.RapidSymbolUsageSearcher;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.find.usages.api.PsiUsage;
import com.intellij.refactoring.rename.api.RenameUsage;
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters;
import com.intellij.refactoring.rename.api.RenameUsageSearcher;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RapidRenameUsageSearcher implements RenameUsageSearcher {

    @NotNull
    @Override
    public Collection<? extends Query<? extends RenameUsage>> collectSearchRequests(@NotNull RenameUsageSearchParameters parameters) {
        if (!(parameters.getTarget() instanceof RapidSymbolRenameTarget target)) {
            return List.of();
        }
        PhysicalSymbol symbol = target.getSymbol();
        String name = symbol.getName();
        if (name == null) {
            return List.of();
        }
        Query<? extends PsiUsage> query = RapidSymbolUsageSearcher.collectSearchRequests(symbol, name, parameters.getProject(), parameters.getSearchScope());
        return List.of(query.mapping(usage -> new RapidSymbolModifiableRenameUsage(symbol, usage)));
    }
}
