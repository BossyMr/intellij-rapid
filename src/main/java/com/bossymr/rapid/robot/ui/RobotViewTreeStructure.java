package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.robot.ui.node.RobotViewListNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RobotViewTreeStructure extends AbstractTreeStructureBase {

    private final AbstractTreeNode<?> rootNode;

    public RobotViewTreeStructure(@NotNull Project project) {
        super(project);
        this.rootNode = createRoot(project);
    }

    @Override
    public @Nullable List<TreeStructureProvider> getProviders() {
        return null;
    }

    protected AbstractTreeNode<?> createRoot(@NotNull Project project) {
        return new RobotViewListNode(project);
    }

    @Override
    public @NotNull Object getRootElement() {
        return rootNode;
    }

    @Override
    public void commit() {}

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }
}
