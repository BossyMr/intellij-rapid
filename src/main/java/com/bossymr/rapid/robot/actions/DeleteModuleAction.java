package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.intellij.openapi.progress.Task.Backgroundable;

public class DeleteModuleAction extends RobotContextAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) {
            return;
        }
        NetworkManager manager = robot.getNetworkManager();
        if (manager == null) {
            return;
        }
        TaskService taskService = manager.createService(TaskService.class);
        List<PhysicalModule> modules = getModules(e);
        WriteAction.runAndWait(() -> {
            for (PhysicalModule module : modules) {
                String name = module.getName();
                if (name == null) {
                    continue;
                }
                RapidTask task = getTask(project, robot, module);
                if (task == null) {
                    continue;
                }
                try {
                    Task remoteTask = taskService.getTask(task.getName()).get();
                    remoteTask.unloadModule(name).get();
                    module.delete();
                } catch (IOException ex) {
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        new Backgroundable(project, RapidBundle.message("robot.download.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance();
                RapidRobot robot = service.getRobot();
                if (robot != null) {
                    try {
                        robot.download();
                    } catch (IOException | InterruptedException ignored) {}
                }
            }
        }.queue();
    }

    private @Nullable RapidTask getTask(@NotNull Project project, @NotNull RapidRobot robot, @NotNull PhysicalModule module) {
        for (RapidTask task : robot.getTasks()) {
            Set<PhysicalModule> modules = task.getModules(project);
            for (PhysicalModule physicalModule : modules) {
                if (physicalModule.isEquivalentTo(module)) {
                    return task;
                }
            }
        }
        return null;
    }

    private @NotNull List<PhysicalModule> getModules(@NotNull AnActionEvent event) {
        Object[] selectedItems = PlatformCoreDataKeys.SELECTED_ITEMS.getData(event.getDataContext());
        if (selectedItems == null) {
            return List.of();
        }
        List<PhysicalModule> modules = new ArrayList<>();
        for (Object selectedItem : selectedItems) {
            if (selectedItem instanceof PhysicalModule module) {
                modules.add(module);
            }
        }
        return modules;
    }

    @Override
    protected boolean isAvailable(@NotNull AnActionEvent event, @NotNull Object selected) {
        return selected instanceof RapidModule;
    }
}
