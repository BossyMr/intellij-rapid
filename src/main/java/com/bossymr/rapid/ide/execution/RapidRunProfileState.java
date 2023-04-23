package com.bossymr.rapid.ide.execution;

import com.bossymr.network.NetworkManager;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfigurationOptions;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.filter.RapidFileFilter;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
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
            throw new ExecutionException(RapidBundle.message("run.execution.robot.not.connected", path));
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
    public @NotNull NetworkManager getNetworkManager() throws IOException, InterruptedException {
        NetworkManager manager = robot.getNetworkManager();
        if (manager == null) {
            return robot.reconnect();
        }
        return manager;
    }

    public @NotNull NetworkManager setupExecution() throws IOException, InterruptedException {
        NetworkManager manager = getNetworkManager();
        for (TaskState state : states) {
            if (state.getName() == null) continue;
            if (state.getModuleName() != null) {
                upload(state.getName(), state.getModuleName());
            }
            activate(manager, state, state.getName());
        }
        robot.download();
        return manager;
    }

    private void upload(@NotNull String taskName, @NotNull String moduleName) throws IOException, InterruptedException {
        RapidTask task = robot.getTask(taskName);
        if (task != null) {
            Module module = ModuleManager.getInstance(getProject()).findModuleByName(moduleName);
            if (module != null) {
                Collection<VirtualFile> modules = ReadAction.compute(() -> FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope()));
                getRobot().upload(task, modules.stream().map(file -> file.toNioPath().toFile()).collect(Collectors.toSet()));
            }
        }
    }

    private void activate(@NotNull NetworkManager manager, @NotNull TaskState taskState, @NotNull String taskName) throws IOException, InterruptedException {
        try (NetworkManager action = manager.createLight()) {
            Task task = action.createService(TaskService.class).getTask(taskName).get();
            switch (task.getActivityState()) {
                case ENABLED -> {
                    if (!taskState.isEnabled()) {
                        task.deactivate().get();
                    }
                }
                case DISABLED -> {
                    if (taskState.isEnabled()) {
                        task.activate().get();
                    }
                }
            }
        }
    }


    @Override
    public @Nullable ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        try {
            NetworkManager manager = setupExecution();
            RapidProcessHandler processHandler = RapidProcessHandler.create(manager);
            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                    .createBuilder(getProject())
                    .filters(new RapidFileFilter(project))
                    .getConsole();
            consoleView.attachToProcess(processHandler);
            return new DefaultExecutionResult(consoleView, processHandler);
        } catch (IOException e) {
            throw new ExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutionException(e);
        }
    }
}
