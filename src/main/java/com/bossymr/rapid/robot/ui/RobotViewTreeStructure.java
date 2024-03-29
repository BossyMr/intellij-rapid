package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.robot.ui.node.RobotViewRobotListNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RobotViewTreeStructure extends AbstractTreeStructureBase {

    private final RobotViewRobotListNode rootNode;

    public RobotViewTreeStructure(@NotNull Project project) {
        super(project);
        this.rootNode = createRoot(project);
    }

    @Override
    public @Nullable List<TreeStructureProvider> getProviders() {
        return null;
    }

    protected RobotViewRobotListNode createRoot(@NotNull Project project) {
        return new RobotViewRobotListNode(project);
    }

    @Override
    public @NotNull RobotViewRobotListNode getRootElement() {
        return rootNode;
    }

    @Override
    public void commit() {}

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }
}
