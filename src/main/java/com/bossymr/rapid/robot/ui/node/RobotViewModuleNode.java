package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.language.symbol.RapidModule;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class RobotViewModuleNode extends RobotViewNode<RapidModule> {

    public RobotViewModuleNode(@NotNull Project project, @NotNull RapidModule node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        TargetPresentation value = getValue().getTargetPresentation();
        Icon icon = value.getIcon();
        if (icon != null) {
            presentation.setIcon(icon);
        }
        presentation.setPresentableText(value.getPresentableText());
        presentation.setLocationString(value.getLocationText());
    }
}
