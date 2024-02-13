package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.language.symbol.RapidModule;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void update(@NotNull PresentationData presentation) {
        RapidModule module = getValue();
        if (module != null) {
            TargetPresentation targetPresentation = module.getTargetPresentation();
            presentation.setIcon(targetPresentation.getIcon());
            presentation.setLocationString(targetPresentation.getLocationText());
            presentation.setPresentableText(targetPresentation.getPresentableText());
        }
    }
}
