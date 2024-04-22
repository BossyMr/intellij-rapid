package com.bossymr.rapid.ide.execution;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfigurationOptions;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.filter.RapidFileFilter;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.MastershipException;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.NetworkAction;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RapidRunProfileState implements RunProfileState {

    private final @NotNull Project project;
    private final @NotNull ExecutorService executorService;
    private final @NotNull CompletableFuture<RapidRobot> robot;
    private final @NotNull List<TaskState> states;

    private RapidRunProfileState(@NotNull Project project, @NotNull ExecutorService executorService, @NotNull CompletableFuture<RapidRobot> robot, @NotNull List<TaskState> states) {
        this.project = project;
        this.executorService = executorService;
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
        if (options.getPath() == null) {
            return null;
        }
        URI path = URI.create(options.getPath());
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture<RapidRobot> completableFuture = new CompletableFuture<>();
        executorService.submit(() -> {
            Credentials credentials;
            if (options.getUsername() != null) {
                credentials = RobotService.DEFAULT_CREDENTIALS;
            } else {
                credentials = RapidRobot.getCredentials(path, options.getUsername());
                if(credentials == null) {
                    credentials = RobotService.DEFAULT_CREDENTIALS;
                }
            }
            if (robot == null || !(path.equals(robot.getPath())) || !(credentials.username().equals(robot.getUsername()))) {
                try {
                    completableFuture.complete(service.connect(path, credentials));
                } catch (Throwable e) {
                    completableFuture.completeExceptionally(e);
                }
            } else {
                completableFuture.complete(robot);
            }
        });
        try {
            return new RapidRunProfileState(project, executorService, completableFuture, options.getTasks());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull CompletableFuture<RapidRobot> getRobot() {
        return robot;
    }

    public @NotNull List<TaskState> getTasks() {
        return states;
    }

    /**
     * Returns a {@code NetworkManager} which is connected to the specified robot. If the robot is not connected,
     * attempts to reconnect, which might fail.
     *
     * @return the {@code NetworkManager}.
     */
    public @NotNull CompletableFuture<NetworkManager> getNetworkManager() {
        return getRobot().thenComposeAsync(robot -> {
            try {
                NetworkManager manager = robot.getNetworkManager() != null ? robot.getNetworkManager() : robot.reconnect();
                return CompletableFuture.completedFuture(manager);
            } catch (Throwable e) {
                return CompletableFuture.failedFuture(new ExecutionException(e));
            }
        });
    }

    public void setupProject(@NotNull RapidRobot robot, @NotNull NetworkManager manager) throws IOException, InterruptedException {
        for (TaskState state : states) {
            if (state.getName() == null) continue;
            if (state.getModuleName() != null) {
                upload(robot, state.getName(), state.getModuleName());
            } else {
                RapidTask task = robot.getTask(state.getName());
                if (task != null) {
                    robot.upload(task);
                }
            }
            activate(manager, state, state.getName());
        }
        robot.download();
    }

    private void upload(@NotNull RapidRobot robot, @NotNull String taskName, @NotNull String moduleName) throws IOException, InterruptedException {
        RapidTask task = robot.getTask(taskName);
        if (task != null) {
            Module module = ModuleManager.getInstance(getProject()).findModuleByName(moduleName);
            if (module != null) {
                Collection<VirtualFile> modules = ReadAction.compute(() -> FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope()));
                robot.upload(task, modules.stream().map(file -> file.toNioPath().toFile()).collect(Collectors.toSet()));
            }
        }
    }

    private void activate(@NotNull NetworkManager manager, @NotNull TaskState taskState, @NotNull String taskName) throws IOException, InterruptedException {
        try (NetworkManager action = new NetworkAction(manager)) {
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
        FileDocumentManager.getInstance().saveAllDocuments();
        RapidProcessHandler processHandler = new RapidProcessHandler(getNetworkManager(), getTasks(), executorService);
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(getProject())
                .filters(new RapidFileFilter(project))
                .getConsole();
        consoleView.attachToProcess(processHandler);
        processHandler.execute(() -> {
            try {
                processHandler.getNetworkManager();
                RapidRobot robot = getRobot().join();
                try {
                    processHandler.setupEventLog();
                    setupProject(robot, processHandler.getNetworkManager());
                    processHandler.setupExecutionState();
                    processHandler.start();
                } catch (MastershipException e) {
                    processHandler.notifyTextAvailable(e.getLocalizedMessage(), ProcessOutputType.STDERR);
                    processHandler.notifyProcessTerminated(1);
                }
            } catch (CancellationException e) {
                throw new ProcessCanceledException();
            } catch (CompletionException e) {
                processHandler.notifyTextAvailable(RapidBundle.message("run.execution.exception"), ProcessOutputType.STDERR);
                processHandler.notifyProcessTerminated(1);
            }
        });
        return new DefaultExecutionResult(consoleView, processHandler);
    }
}
