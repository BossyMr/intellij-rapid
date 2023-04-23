package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void update(@NotNull PresentationData presentation) {
        RapidSymbol value = getValue();
        if (value != null) {
            TargetPresentation targetPresentation = value.getTargetPresentation();
            presentation.setIcon(targetPresentation.getIcon());
            presentation.setLocationString(targetPresentation.getLocationText());
            presentation.setPresentableText(targetPresentation.getPresentableText());
        }
    }
}
