package com.bossymr.rapid.robot.ui.node;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class RobotViewDirectoryNode<T> extends RobotViewNode<Set<T>> {

    private final String title;
    private final Icon icon;

    public RobotViewDirectoryNode(@NotNull Project project, @NotNull String title, @NotNull Icon icon, @NotNull Set<T> values) {
        super(project, values);
        this.title = title;
        this.icon = icon;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> symbols = new ArrayList<>();
        for (T value : getValue()) {
            AbstractTreeNode<?> child = getChild(value);
            symbols.add(child);
        }
        return symbols;
    }

    public abstract @NotNull AbstractTreeNode<?> getChild(@NotNull T value);

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setIcon(icon);
        presentation.setPresentableText(title);
    }
}
