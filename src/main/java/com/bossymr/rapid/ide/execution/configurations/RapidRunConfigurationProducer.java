package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.intellij.util.PopupUtilsKt.getBestPopupPosition;

public class RapidRunConfigurationProducer extends LazyRunConfigurationProducer<RapidRunConfiguration> {

    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
        return RapidConfigurationType.getInstance();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull RapidRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        Location<PsiElement> location = context.getLocation();
        if (location == null) {
            return false;
        }
        RapidRobot robot = RobotService.getInstance().getRobot();
        if (robot == null) {
            return false;
        }
        RapidRunConfigurationOptions options = configuration.getOptions();
        options.setPath(robot.getPath().toString());
        List<TaskState> states = getTasks(robot);
        options.setTasks(states);
        if (states.isEmpty()) {
            return false;
        }
        URI path = robot.getPath();
        if (isRemoteFile(robot, location)) {
            configuration.setName("Run " + path.getHost() + ":" + path.getPort());
            return true;
        }
        Module module = location.getModule();
        if(module == null) {
            return false;
        }
        configuration.setName("Upload and run " + path.getHost() + ":" + path.getPort());
        return true;
    }

    private boolean isRemoteFile(@NotNull RapidRobot robot, @NotNull Location<PsiElement> location) {
        VirtualFile file = location.getVirtualFile();
        for (RapidTask task : robot.getTasks()) {
            Set<VirtualFile> files = task.getVirtualFiles(location.getProject());
            if (files.contains(file)) {
                return true;
            }
        }
        return false;
    }

    private @NotNull List<TaskState> getTasks(@NotNull RapidRobot robot) {
        return robot.getTasks().stream()
                .map(state -> new TaskState(state.getName(), true, null))
                .toList();
    }

    @Override
    public void onFirstRun(@NotNull ConfigurationFromContext configuration, @NotNull ConfigurationContext context, @NotNull Runnable startRunnable) {
        RapidRunConfigurationOptions options = ((RapidRunConfiguration) configuration.getConfiguration()).getOptions();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
           RapidRobot robot = RobotService.getInstance().getRobot();
           if(robot == null) {
               return;
           }
           options.setUsername(robot.getUsername());
           ApplicationManager.getApplication().invokeLater(() -> {
               Location<PsiElement> location = context.getLocation();
               if (location != null) {
                   Module module = location.getModule();
                   if (module != null) {
                       setUploadTask(module.getName(), options, context, startRunnable);
                       return;
                   }
               }
               startRunnable.run();
           }, ModalityState.any());
        });
    }

    private void setUploadTask(@NotNull String name, @NotNull RapidRunConfigurationOptions options, @NotNull ConfigurationContext context, @NotNull Runnable startRunnable) {
        List<TaskState> tasks = options.getTasks();
        if (tasks.size() == 1) {
            tasks.get(0).setModuleName(name);
            ApplicationManager.getApplication().invokeLater(startRunnable);
            return;
        }
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(tasks)
                .setRenderer(new SimpleListCellRenderer<>() {
                    @Override
                    public void customize(@NotNull JList<? extends TaskState> list, TaskState value, int index, boolean selected, boolean hasFocus) {
                        setText(value.getName());
                        setIcon(RapidIcons.RAPID);
                    }
                }).setTitle(RapidBundle.message("run.configuration.producer.select.task", options.getPath()))
                .setAutoselectOnMouseMove(false)
                .setNamerForFiltering(state -> state == null ? "" : state.getName())
                .setMovable(true)
                .setResizable(false)
                .setRequestFocus(true)
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                .setMinSize(JBUI.size(270, 55))
                .setItemsChosenCallback(values -> {
                    if (values.isEmpty()) {
                        return;
                    }
                    values.iterator().next().setModuleName(name);
                    ApplicationManager.getApplication().invokeLater(startRunnable);
                }).createPopup()
                .show(getBestPopupPosition(context.getDataContext()));
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull RapidRunConfiguration configuration, @NotNull ConfigurationContext context) {
        Location<PsiElement> location = context.getLocation();
        if(location == null) {
            return false;
        }
        RapidRunConfigurationOptions options = configuration.getOptions();
        RapidRobot robot = RobotService.getInstance().getRobot();
        if(options.getPath() == null || robot == null) {
            return false;
        }
        try {
            URI path = URI.create(options.getPath());
            if(!(path.equals(robot.getPath()))) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        Module module = location.getModule();
        if(isRemoteFile(robot, location)) {
            return options.getTasks().stream().noneMatch(state -> state.getModuleName() != null);
        } else if (module != null) {
            long count = options.getTasks().stream()
                    .filter(state -> module.getName().equals(state.getModuleName()))
                    .count();
            return count == 1 && options.getTasks().stream().noneMatch(state -> state.getModuleName() != null && !(state.getModuleName().equals(module.getName())));
        } else {
            return false;
        }
    }
}
