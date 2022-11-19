package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class RobotViewSymbolNode extends RobotViewNode<RapidSymbol> {

    public RobotViewSymbolNode(@NotNull Project project, @NotNull RapidSymbol node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        ItemPresentation value = getValue().getPresentation();
        if (value != null) {
            Icon icon = value.getIcon(false);
            if (icon != null) {
                presentation.setIcon(icon);
            }
            presentation.setPresentableText(value.getPresentableText());
            presentation.setLocationString(value.getLocationString());
        }
    }
}
