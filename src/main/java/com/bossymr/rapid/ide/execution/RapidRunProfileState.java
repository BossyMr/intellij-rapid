package com.bossymr.rapid.ide.execution;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.network.RobotService;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

public class RapidRunProfileState implements RunProfileState {

    private final @NotNull Project project;

    private final @NotNull RapidRobot robot;
    private final @NotNull RapidTask task;
    private final @Nullable Module module;

    public RapidRunProfileState(@NotNull Project project, @NotNull RapidRobot robot, @NotNull RapidTask task, @Nullable Module module) {
        this.project = project;
        this.robot = robot;
        this.task = task;
        this.module = module;
    }

    public @NotNull RapidRobot getRobot() {
        return robot;
    }

    public @NotNull RapidTask getTask() {
        return task;
    }

    public @Nullable Module getModule() {
        return module;
    }

    public @NotNull RobotService getRobotService() throws IOException, InterruptedException {
        RobotService robotService = robot.getRobotService();
        if (robotService == null) {
            robotService = robot.reconnect();
        }
        upload();
        return robotService;
    }

    public void upload() throws IOException, InterruptedException {
        if (module != null) {
            Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleScope());
            robot.upload(task, virtualFiles);
        }
    }

    @Override
    public @Nullable ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        try {
            RobotService robotService = getRobotService();
            RapidProcessHandler processHandler = new RapidProcessHandler(robotService);
            processHandler.startProcess();
            ConsoleView consoleView = getBuilder().getConsole();
            consoleView.attachToProcess(processHandler);
            return new DefaultExecutionResult(consoleView, processHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutionException(e);
        }
    }

    private @NotNull TextConsoleBuilder getBuilder() {
        if (module != null) {
            return TextConsoleBuilderFactory.getInstance().createBuilder(project, module.getModuleScope());
        } else {
            return TextConsoleBuilderFactory.getInstance().createBuilder(project);
        }
    }
}
