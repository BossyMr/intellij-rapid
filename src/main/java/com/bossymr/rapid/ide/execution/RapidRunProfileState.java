package com.bossymr.rapid.ide.execution;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.util.concurrency.annotations.RequiresWriteLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RapidRunProfileState implements RunProfileState {

    private final @NotNull Project project;
    private final @NotNull RapidRobot robot;
    private final @NotNull List<TaskState> taskStates;

    public RapidRunProfileState(@NotNull Project project, @NotNull RapidRobot robot, @NotNull List<TaskState> taskStates) {
        this.project = project;
        this.robot = robot;
        this.taskStates = taskStates;
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull RapidRobot getRobot() {
        return robot;
    }

    public @NotNull List<TaskState> getTaskStates() {
        return taskStates;
    }

    public @NotNull CompletableFuture<@NotNull NetworkEngine> getNetworkEngine() {
        NetworkEngine networkEngine = robot.getNetworkEngine();
        if (networkEngine == null) {
            return robot.reconnect();
        }
        return CompletableFuture.completedFuture(networkEngine);
    }

    @RequiresWriteLock
    public @NotNull CompletableFuture<NetworkEngine> setupExecution() {
        return getNetworkEngine()
                .thenComposeAsync(engine -> {
                    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                    for (TaskState taskState : taskStates) {
                        if (taskState.getName() != null) {
                            if (taskState.getModuleName() != null) {
                                RapidTask task = robot.getTask(taskState.getName());
                                if (task == null) continue;
                                Module module = ModuleManager.getInstance(getProject()).findModuleByName(taskState.getModuleName());
                                if (module == null) continue;
                                Collection<VirtualFile> modules = FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope());
                                completableFutures.add(getRobot().upload(task, Set.copyOf(modules)));
                            }
                            TaskService taskService = engine.createService(TaskService.class);
                            completableFutures.add(taskService.getTask(taskState.getName()).sendAsync()
                                    .thenComposeAsync(task -> {
                                        if (taskState.isEnabled() && task.getActivityState() == TaskActiveState.DISABLED) {
                                            return task.activate().sendAsync();
                                        } else if (!(taskState.isEnabled()) && task.getActivityState() == TaskActiveState.ENABLED) {
                                            return task.deactivate().sendAsync();
                                        } else {
                                            return CompletableFuture.completedFuture(null);
                                        }
                                    }));
                        }
                    }
                    return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new))
                            .thenComposeAsync(unused -> robot.download())
                            .thenApplyAsync(unused -> engine);
                });
    }


    @Override
    public @Nullable ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        CompletableFuture<NetworkEngine> completableFuture = setupExecution();
        RapidProcessHandler processHandler = new RapidProcessHandler(completableFuture);
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(getProject()).getConsole();
        consoleView.attachToProcess(processHandler);
        return new DefaultExecutionResult(consoleView, processHandler);
    }
}
