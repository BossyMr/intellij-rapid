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
import java.util.Optional;

public class RobotViewRobotListNode extends RobotViewNode<Project> {

    public RobotViewRobotListNode(@NotNull Project project) {
        super(project, project);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<RobotViewRobotNode> nodes = new ArrayList<>();
        RobotService service = RobotService.getInstance(getProject());
        Optional<Robot> robot = service.getRobot();
        robot.ifPresent(value -> nodes.add(new RobotViewRobotNode(getProject(), value)));
        return nodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {}
}
