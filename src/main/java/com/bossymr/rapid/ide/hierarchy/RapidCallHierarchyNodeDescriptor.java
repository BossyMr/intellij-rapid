package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.psi.FormatUtil;
import com.bossymr.rapid.language.psi.FormatUtil.Option;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;

public class RapidCallHierarchyNodeDescriptor extends HierarchyNodeDescriptor implements Navigatable {

    public RapidCallHierarchyNodeDescriptor(@NotNull Project project, @Nullable NodeDescriptor parentDescriptor, @NotNull PsiElement element, boolean isBase) {
        super(project, parentDescriptor, element, isBase);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean update() {
        CompositeAppearance oldText = myHighlightedText;
        Icon oldIcon = getIcon();
        boolean changes = super.update();
        RapidSymbol element = getContextElement();
        if (element == null) {
            return invalidElement();
        }
        TargetPresentation targetPresentation = element.getTargetPresentation();
        installIcon(targetPresentation.getIcon(), changes);
        myHighlightedText = new CompositeAppearance();
        TextAttributes textAttributes = null;
        if (myColor != null) {
            textAttributes = new TextAttributes(myColor, null, null, null, Font.PLAIN);
        }
        String name = element instanceof RapidRoutine routine ?
                FormatUtil.format(routine, EnumSet.of(Option.SHOW_MODE, Option.SHOW_NAME, Option.SHOW_PARAMETERS), EnumSet.of(Option.SHOW_TYPE))
                : element.getPresentableName();
        myHighlightedText.getEnding().addText(name, textAttributes);
        if (element instanceof PhysicalSymbol physicalSymbol) {
            PhysicalModule module = PsiTreeUtil.getParentOfType(physicalSymbol, PhysicalModule.class);
            if (module != null) {
                myHighlightedText.getEnding().addText(" (" + module.getPresentableName() + ")", HierarchyNodeDescriptor.getPackageNameAttributes());
            }
        }
        myName = myHighlightedText.getText();
        changes |= !(Comparing.equal(myHighlightedText, oldText)) || !(Comparing.equal(getIcon(), oldIcon));
        return changes;
    }

    public @Nullable RapidSymbol getContextElement() {
        PsiElement symbol = getPsiElement();
        if (symbol instanceof RapidFakeVirtualSymbol element) {
            return element.getSymbol();
        }
        if (symbol == null) {
            return null;
        }
        return PsiTreeUtil.getNonStrictParentOfType(symbol, PhysicalField.class, PhysicalRoutine.class);
    }

    @Override
    public boolean isValid() {
        return getContextElement() != null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        PsiElement element = getPsiElement();
        if (element instanceof Navigatable navigatable && navigatable.canNavigate()) {
            navigatable.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        PsiElement element = getPsiElement();
        return element instanceof Navigatable navigatable && navigatable.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }
}
