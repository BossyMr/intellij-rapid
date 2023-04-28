package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.model.search.SearchService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidCallerTreeStructure extends HierarchyTreeStructure {

    private final @NotNull String scopeType;

    protected RapidCallerTreeStructure(@NotNull Project project, @NotNull PhysicalSymbol symbol, @NotNull String scopeType) {
        super(project, new RapidCallHierarchyNodeDescriptor(project, null, symbol, true));
        this.scopeType = scopeType;
    }

    protected @NotNull Project getProject() {
        return myProject;
    }

    @Override
    protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        if (!(descriptor instanceof RapidCallHierarchyNodeDescriptor nodeDescriptor)) {
            throw new IllegalArgumentException();
        }
        RapidSymbol symbol = nodeDescriptor.getContextElement();
        HierarchyNodeDescriptor baseDescriptor = getBaseDescriptor();
        PsiElement element = nodeDescriptor.getPsiElement();
        if (element == null | symbol == null || symbol.getName() == null || baseDescriptor == null) {
            return new Object[0];
        }
        if (nodeDescriptor.isRecursive(symbol)) {
            return new Object[0];
        }
        SearchScope searchScope = getSearchScope(scopeType, element);
        return SearchService.getInstance().searchPsiSymbolReferences(getProject(), symbol, searchScope)
                .findAll().stream()
                .distinct()
                .map(result -> new RapidCallHierarchyNodeDescriptor(getProject(), nodeDescriptor, result.getElement(), false))
                .toArray();
    }
}
