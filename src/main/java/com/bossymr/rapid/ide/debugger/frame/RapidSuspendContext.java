package com.bossymr.rapid.ide.debugger.frame;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.ide.debugger.RapidDebugProcess;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RapidSuspendContext extends XSuspendContext {

    private final @NotNull RapidExecutionStack executionStack;
    private final @NotNull NetworkManager manager;
    private final @NotNull Project project;
    private final @NotNull List<CompletableFuture<Breakpoint>> breakpoints;

    public RapidSuspendContext(@NotNull Project project, @NotNull Set<XBreakpoint<?>> breakpoints, @NotNull NetworkManager manager, @NotNull Task task, @NotNull StackFrame stackFrame) {
        this.manager = manager;
        this.project = project;
        this.breakpoints = breakpoints.stream()
                .map(breakpoint -> breakpoint.getUserData(RapidDebugProcess.BREAKPOINT_KEY))
                .toList();
        this.executionStack = new RapidExecutionStack(project, task, stackFrame, isAtBreakpoint(task, stackFrame), true);
    }

    private boolean isAtBreakpoint(@NotNull Task task, @NotNull StackFrame stackFrame) {
        for (CompletableFuture<Breakpoint> completableFuture : breakpoints) {
            Breakpoint breakpoint = completableFuture.getNow(null);
            if (breakpoint == null) continue;
            String breakpointTask = breakpoint.getLink("self").getPath().substring(1).split("/")[3];
            if (task.getName().equals(breakpointTask)) {
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
        TaskService taskService = manager.createService(TaskService.class);
        taskService.getTasks().sendAsync().thenAcceptAsync(tasks -> {
            List<RapidExecutionStack> executionStacks = new ArrayList<>();
            List<CompletableFuture<?>> requests = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getActivityState() == TaskActiveState.DISABLED) continue;
                requests.add(task.getStackFrame(1).sendAsync().thenAcceptAsync(stackFrame -> {
                    executionStacks.add(new RapidExecutionStack(project, task, stackFrame, isAtBreakpoint(task, stackFrame), task.equals(executionStack.getTask())));
                }));
            }
            CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new))
                    .thenRunAsync(() -> container.addExecutionStack(executionStacks, true));
        });
    }
}
