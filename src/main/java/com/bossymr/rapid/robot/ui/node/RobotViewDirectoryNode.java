package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidSymbol;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RobotViewDirectoryNode extends RobotViewNode<Set<RapidSymbol>> {

    public RobotViewDirectoryNode(@NotNull Project project, @NotNull Set<RapidSymbol> node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<RobotViewSymbolNode> symbols = new ArrayList<>();
        for (RapidSymbol symbol : getValue()) {
            symbols.add(new RobotViewSymbolNode(getProject(), symbol));
        }
        return symbols;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setIcon(RapidIcons.ROBOT_DIRECTORY);
        presentation.setPresentableText("Symbol");
    }
}
