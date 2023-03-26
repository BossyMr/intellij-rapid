package com.bossymr.rapid.ide.execution;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfigurationOptions;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.filter.RapidFileFilter;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RapidRunProfileState implements RunProfileState {

    private final @NotNull Project project;
    private final @NotNull RapidRobot robot;
    private final @NotNull List<TaskState> states;

    private RapidRunProfileState(@NotNull Project project, @NotNull RapidRobot robot, @NotNull List<TaskState> states) {
        this.project = project;
        this.robot = robot;
        this.states = states;
    }

    /**
     * Attempts to create a new {@code RunProfileState}.
     *
     * @param project the project.
     * @param options the options.
     * @return the {@code RunProfileState}, or {@code null} if it is impossible to start the process.
     * @throws ExecutionException if an error occurred while setting up the {@code RunProfileState}.
     * @see RunConfiguration#getState(Executor, ExecutionEnvironment)
     */
    public static @Nullable RapidRunProfileState create(@NotNull Project project, @NotNull RapidRunConfigurationOptions options) throws ExecutionException {
        RemoteRobotService service = RemoteRobotService.getInstance();
        RapidRobot robot = service.getRobot().getNow(null);
        if (robot == null) {
            return null;
        }
        if (options.getRobotPath() == null) {
            return null;
        }
        try {
            URI path = URI.create(options.getRobotPath());
            if (robot.getPath().equals(path)) {
                return new RapidRunProfileState(project, robot, options.getRobotTasks());
            }
            // TODO: 2023-03-16 If the current robot is not the specified robot, offer an opportunity to set credentials to connect to the specified robot.
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull RapidRobot getRobot() {
        return robot;
    }

    public @NotNull List<TaskState> getTasks() {
        return states;
    }

    /**
     * Returns a {@code NetworkEngine} which is connected to the specified robot. If the robot is not connected,
     * attempts to reconnect, which might fail.
     *
     * @return an asynchronous request which will complete with a {@code NetworkEngine} connected to the robot.
     */
    public @NotNull CompletableFuture<@NotNull NetworkEngine> getNetworkEngine() {
        NetworkEngine networkEngine = robot.getNetworkEngine();
        if (networkEngine == null) {
            return robot.reconnect();
        }
        return CompletableFuture.completedFuture(networkEngine);
    }

    public @NotNull CompletableFuture<NetworkEngine> setupExecution() {
        return getNetworkEngine().thenComposeAsync(engine -> {
            List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
            for (TaskState taskState : states) {
                if (taskState.getName() == null) continue;
                if (taskState.getModuleName() != null) {
                    completableFutures.add(upload(taskState.getName(), taskState.getModuleName()));
                }
                completableFutures.add(activate(engine, taskState, taskState.getName()));
            }
            return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new))
                    .thenComposeAsync(unused -> robot.download())
                    .thenApplyAsync(unused -> engine);
        });
    }

    private @NotNull CompletableFuture<Void> upload(@NotNull String taskName, @NotNull String moduleName) {
        RapidTask task = robot.getTask(taskName);
        if (task != null) {
            Module module = ModuleManager.getInstance(getProject()).findModuleByName(moduleName);
            if (module != null) {
                Collection<VirtualFile> modules = ReadAction.compute(() -> FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope()));
                return getRobot().upload(task, Set.copyOf(modules));
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private @NotNull CompletableFuture<Void> activate(@NotNull NetworkEngine engine, @NotNull TaskState taskState, @NotNull String taskName) {
        return engine.createService(TaskService.class).getTask(taskName).sendAsync()
                .thenComposeAsync(task -> {
                    /*
                     * Check if the task needs to be activated or deactivated.
                     */
                    return switch (task.getActivityState()) {
                        case ENABLED -> {
                            if (!taskState.isEnabled()) {
                                yield task.deactivate().sendAsync();
                            }
                            yield CompletableFuture.completedFuture(null);
                        }
                        case DISABLED -> {
                            if (taskState.isEnabled()) {
                                yield task.activate().sendAsync();
                            }
                            yield CompletableFuture.completedFuture(null);
                        }
                    };
                });
    }


    @Override
    public @Nullable ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        CompletableFuture<NetworkEngine> completableFuture = setupExecution();
        RapidProcessHandler processHandler = new RapidProcessHandler(completableFuture);
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(getProject())
                .filters(new RapidFileFilter(project))
                .getConsole();
        consoleView.attachToProcess(processHandler);
        return new DefaultExecutionResult(consoleView, processHandler);
    }
}
