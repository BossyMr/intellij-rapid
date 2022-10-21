package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class RobotViewRobotNode extends RobotViewNode<Robot> {

    public RobotViewRobotNode(@NotNull Project project, @NotNull Robot node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.singletonList(new RobotViewDirectoryNode(getProject(), RobotService.getInstance(getProject()).getSymbols()));
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setIcon(RapidIcons.ROBOT_ICON);
        presentation.setPresentableText(getValue().getName());
    }
}
