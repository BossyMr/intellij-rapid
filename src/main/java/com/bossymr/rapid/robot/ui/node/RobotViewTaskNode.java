package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class RobotViewTaskNode extends RobotViewNode<RapidTask> {

    public RobotViewTaskNode(@NotNull Project project, @NotNull RapidTask node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        if (getValue() == null || !(getValue().isValid())) {
            return Collections.emptyList();
        }
        Set<PhysicalModule> modules = getValue().getModules(getProject());
        return modules.stream()
                .map(module -> new RobotViewModuleNode(getProject(), module))
                .toList();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        if (getValue() != null) {
            presentation.setIcon(RapidIcons.TASK);
            presentation.setPresentableText(getValue().getName());
        }
    }
}
