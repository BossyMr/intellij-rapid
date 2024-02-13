package com.bossymr.rapid.ide.navigation;

import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidClassNavigationContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        StubIndex.getInstance().processAllKeys(RapidModuleIndex.KEY, processor, scope, filter);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<? super NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        StubIndex.getInstance().processElements(RapidModuleIndex.KEY, name, parameters.getProject(), parameters.getSearchScope(), parameters.getIdFilter(), PhysicalModule.class, processor);
    }
}
