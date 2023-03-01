package com.bossymr.rapid.ide.debugger.frame;

import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidSuspendContext extends XSuspendContext {

    private final @NotNull RapidExecutionStack executionStack;

    public RapidSuspendContext(@NotNull Project project, @NotNull Task task) {
        this.executionStack = new RapidExecutionStack(project, task);
    }

    @Override
    public @Nullable RapidExecutionStack getActiveExecutionStack() {
        return executionStack;
    }
}
