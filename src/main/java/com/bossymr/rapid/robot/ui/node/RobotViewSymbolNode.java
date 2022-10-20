package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.language.psi.RapidSymbol;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
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

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setIcon(getValue().getIcon(0));
        presentation.setPresentableText(getValue().getName());
    }
}
