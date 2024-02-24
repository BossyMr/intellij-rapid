package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.Map;

public class RapidCallHierarchyBrowser extends CallHierarchyBrowserBase {

    public RapidCallHierarchyBrowser(@NotNull Project project, @NotNull PsiElement method) {
        super(project, method);
    }

    protected @NotNull Project getProject() {
        return myProject;
    }

    @Override
    protected @Nullable PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
        return null;
    }

    @Override
    protected void createTrees(@NotNull Map<? super @Nls String, ? super JTree> trees) {
        BaseOnThisMethodAction action = new BaseOnThisMethodAction();
        trees.put(getCalleeType(), createTree(action));
        trees.put(getCallerType(), createTree(action));
    }

    private @NotNull JTree createTree(@NotNull BaseOnThisMethodAction action) {
        JTree tree = createTree(false);
        PopupHandler.installPopupMenu(tree, IdeActions.GROUP_CALL_HIERARCHY_POPUP, ActionPlaces.METHOD_HIERARCHY_VIEW_POPUP);
        action.registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_CALL_HIERARCHY).getShortcutSet(), tree);
        return tree;
    }

    @Override
    protected boolean isApplicableElement(@NotNull PsiElement element) {
        if (element instanceof PhysicalRoutine) {
            return true;
        }
        if (element instanceof PhysicalField field) {
            PhysicalRoutine routine = PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class, true);
            // Check if the field is a local field.
            return routine == null;
        }
        return false;
    }

    @Override
    protected @Nullable HierarchyTreeStructure createHierarchyTreeStructure(@NotNull String type, @NotNull PsiElement element) {
        if (getCallerType().equals(type)) {
            return new RapidCallerTreeStructure(getProject(), (PhysicalSymbol) element, getCurrentScopeType());
        }
        if (getCalleeType().equals(type)) {
            return new RapidCalleeTreeStructure(getProject(), (PhysicalSymbol) element);
        }
        return null;
    }

    @Override
    protected @Nullable Comparator<NodeDescriptor<?>> getComparator() {
        HierarchyBrowserManager manager = HierarchyBrowserManager.getInstance(getProject());
        HierarchyBrowserManager.State state = manager.getState();
        return state != null && state.SORT_ALPHABETICALLY ? AlphaComparator.INSTANCE : null;
    }
}
