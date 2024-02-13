package com.bossymr.rapid.robot.ui.node;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class RobotViewNode<T> extends AbstractTreeNode<T> {

    protected RobotViewNode(@NotNull Project project, @NotNull T node) {
        super(project, node);
    }

    @Override
    public VirtualFile getVirtualFile() {
        return super.getVirtualFile();
    }


}
