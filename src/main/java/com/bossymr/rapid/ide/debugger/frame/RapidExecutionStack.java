package com.bossymr.rapid.ide.debugger.frame;

import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XExecutionStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RapidExecutionStack extends XExecutionStack {

    private final @NotNull Project project;
    private final @NotNull Task task;
    private @NotNull List<RapidStackFrame> stackFrames = new ArrayList<>();

    public RapidExecutionStack(@NotNull Project project, @NotNull Task task) {
        super(task.getName(), AllIcons.Debugger.ThreadCurrent);
        this.project = project;
        this.task = task;
    }

    @Override
    public @Nullable RapidStackFrame getTopFrame() {
        if (stackFrames.isEmpty()) {
            return null;
        }
        return stackFrames.get(0);
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

}
