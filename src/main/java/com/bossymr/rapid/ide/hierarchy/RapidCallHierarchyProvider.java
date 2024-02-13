package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidCallHierarchyProvider implements HierarchyProvider {

    @Override
    public @Nullable PsiElement getTarget(@NotNull DataContext dataContext) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return null;
        }
        PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        PhysicalRoutine routine = PsiTreeUtil.getParentOfType(element, PhysicalRoutine.class, false);
        if (element instanceof PhysicalField field) {
            if (routine == null) {
                return field;
            }
        }
        return routine;
    }

    @Override
    public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
        if (!(target instanceof PhysicalSymbol symbol)) {
            throw new IllegalArgumentException();
        }
        return new RapidCallHierarchyBrowser(target.getProject(), symbol);
    }

    @Override
    public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
        if (!(hierarchyBrowser instanceof RapidCallHierarchyBrowser callHierarchyBrowser)) {
            throw new IllegalArgumentException();
        }
        callHierarchyBrowser.changeView(RapidCallHierarchyBrowser.getCalleeType());
    }
}
