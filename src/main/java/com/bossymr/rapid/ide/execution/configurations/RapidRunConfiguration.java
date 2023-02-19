package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.ui.RapidRunConfigurationEditor;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public class RapidRunConfiguration extends ModuleBasedConfiguration<RapidRunConfigurationModule, Element> {

    public RapidRunConfiguration(@Nullable String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(name, new RapidRunConfigurationModule(project), factory);
    }

    @Override
    public @NotNull RapidRunConfigurationOptions getOptions() {
        return (RapidRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull Collection<Module> getValidModules() {
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        return List.of(modules);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RapidRunConfigurationEditor(getProject());
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        RemoteRobotService robotService = RemoteRobotService.getInstance();
        RapidRobot robot = robotService.getRobot();
        if (robot == null || getOptions().getRobotPath() == null) return null;
        URI robotPath = URI.create(getOptions().getRobotPath());
        if (!(robotPath.equals(robot.getPath()))) return null;
        List<RapidTask> tasks = robot.getTasks();
        for (RapidTask task : tasks) {
            if (task.getName().equals(getOptions().getTaskName())) {
                return new RapidRunProfileState(getProject(), robot, task, getConfigurationModule().getModule());
            }
        }
        return null;
    }
}
