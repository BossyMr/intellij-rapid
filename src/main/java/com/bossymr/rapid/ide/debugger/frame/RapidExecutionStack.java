package com.bossymr.rapid.ide.debugger.frame;

import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskExecutionState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RapidExecutionStack extends XExecutionStack {

    private final @NotNull Project project;
    private final @NotNull Task task;
    private @NotNull List<RapidStackFrame> stackFrames = new ArrayList<>();

    public RapidExecutionStack(@NotNull Project project, @NotNull Task task, @NotNull StackFrame stackFrame, boolean isAtBreakpoint, boolean current) {
        super(task.getName() + ": " + stackFrame.getExecutionLevel(), getIcon(task, isAtBreakpoint, current));
        this.stackFrames.add(new RapidStackFrame(project, stackFrame));
        this.project = project;
        this.task = task;
    }

    private static @NotNull Icon getIcon(@NotNull Task task, boolean isAtBreakpoint, boolean current) {
        boolean isRunning = task.getExecutionState() == TaskExecutionState.STARTED;
        if (current) {
            return isRunning ? AllIcons.Debugger.ThreadRunning : AllIcons.Debugger.ThreadCurrent;
        }
        if (isAtBreakpoint) {
            return AllIcons.Debugger.ThreadAtBreakpoint;
        }
        if (!(isRunning)) {
            return AllIcons.Debugger.ThreadSuspended;
        }
        return AllIcons.Debugger.ThreadRunning;
    }

    @Override
    public @Nullable RapidStackFrame getTopFrame() {
        if (stackFrames.isEmpty()) {
            return null;
        }
        return stackFrames.get(0);
    }

    public @NotNull Task getTask() {
        return task;
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, @NotNull XStackFrameContainer container) {
        if (container.isObsolete()) {
            return;
        }
        stackFrames = new ArrayList<>();
        getStackFrameAsync(firstFrameIndex, firstFrameIndex + 1, container);
    }

    private @NotNull CompletableFuture<Void> getStackFrameAsync(int firstFrameIndex, int index, @NotNull XStackFrameContainer container) {
        return task.getStackFrame(index).sendAsync()
                .thenComposeAsync(stackFrame -> {
                    if (stackFrame.getStartRow() == 0) {
                        container.addStackFrames(stackFrames.subList(firstFrameIndex, stackFrames.size()), true);
                        return CompletableFuture.completedFuture(null);
                    }
                    stackFrames.add(new RapidStackFrame(project, stackFrame));
                    return getStackFrameAsync(firstFrameIndex, index + 1, container);
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidExecutionStack that = (RapidExecutionStack) o;
        return getTask().equals(that.getTask());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTask());
    }

    @Override
    public String toString() {
        return "RapidExecutionStack{" +
                "task=" + task +
                '}';
    }
}
