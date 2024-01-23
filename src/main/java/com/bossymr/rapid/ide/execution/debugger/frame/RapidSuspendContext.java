package com.bossymr.rapid.ide.execution.debugger.frame;

import com.bossymr.network.NetworkManager;
import com.bossymr.rapid.ide.execution.debugger.RapidDebugProcess;
import com.bossymr.rapid.ide.execution.debugger.RapidDebugProcess.BreakpointEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RapidSuspendContext extends XSuspendContext {

    private final @NotNull RapidExecutionStack executionStack;
    private final @NotNull RapidDebugProcess process;
    private final @NotNull Project project;
    private final @NotNull List<BreakpointEntity> breakpoints;

    public RapidSuspendContext(@NotNull Project project, @NotNull Set<XBreakpoint<?>> breakpoints, @NotNull RapidDebugProcess process, @NotNull Task task, @NotNull StackFrame stackFrame) {
        this.process = process;
        this.project = project;
        this.breakpoints = breakpoints.stream()
                                      .map(breakpoint -> breakpoint.getUserData(RapidDebugProcess.BREAKPOINT_KEY))
                                      .filter(Objects::nonNull)
                                      .toList();
        this.executionStack = new RapidExecutionStack(process, project, task, stackFrame, isAtBreakpoint(task, stackFrame), true);
    }

    private boolean isAtBreakpoint(@NotNull Task task, @NotNull StackFrame stackFrame) {
        for (BreakpointEntity breakpointEntity : breakpoints) {
            String taskName = breakpointEntity.taskName();
            if (task.getName().equals(taskName)) {
                Breakpoint breakpoint = breakpointEntity.breakpoint();
                if (breakpoint.getStartRow() <= stackFrame.getStartRow() && breakpoint.getEndRow() >= stackFrame.getEndRow()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull RapidExecutionStack getActiveExecutionStack() {
        return executionStack;
    }

    @Override
    public void computeExecutionStacks(@NotNull XExecutionStackContainer container) {
        process.execute(() -> {
            TaskService taskService = process.getManager().createService(TaskService.class);
            List<Task> tasks = taskService.getTasks().get();
            List<RapidExecutionStack> executionStacks = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getActivityState() == TaskActiveState.DISABLED) continue;
                StackFrame stackFrame = task.getStackFrame(1).get();
                RapidExecutionStack stack = new RapidExecutionStack(process, project, task, stackFrame, isAtBreakpoint(task, stackFrame), task.equals(executionStack.getTask()));
                executionStacks.add(stack);
            }
            container.addExecutionStack(executionStacks, true);
        });
    }
}
