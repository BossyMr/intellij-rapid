package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RobotViewRobotListNode extends RobotViewNode<Project> {

    public RobotViewRobotListNode(@NotNull Project project) {
        super(project, project);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<RobotViewRobotNode> nodes = new ArrayList<>();
        RobotService service = RobotService.getInstance();
        Robot robot = service.getRobot();
        if (robot != null) {
            nodes.add(new RobotViewRobotNode(getProject(), robot));
        }
        return nodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {}
}
