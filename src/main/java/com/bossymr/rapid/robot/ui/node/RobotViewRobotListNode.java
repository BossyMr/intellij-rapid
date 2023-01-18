package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.intellij.ide.projectView.PresentationData;
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
    public @NotNull Collection<RobotViewRobotNode> getChildren() {
        List<RobotViewRobotNode> nodes = new ArrayList<>();
        RemoteRobotService service = RemoteRobotService.getInstance();
        Robot robot = service.getRobot();
        if (robot != null) {
            nodes.add(new RobotViewRobotNode(getProject(), robot));
        }
        return nodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {}
}
