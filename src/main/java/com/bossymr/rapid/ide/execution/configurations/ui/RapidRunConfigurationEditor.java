package com.bossymr.rapid.ide.execution.configurations.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.RobotEventListener;
import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class RapidRunConfigurationEditor extends SettingsEditor<RapidRunConfiguration> {

    private final ModulesComboBox moduleComboBox;
    private final ComboBox<RapidRobot> robotComboBox;
    private final ComboBox<RapidTask> taskComboBox;

    private final JPanel panel;

    private final Project project;

    public RapidRunConfigurationEditor(@NotNull Project project) {
        this.project = project;
        this.moduleComboBox = new ModulesComboBox();
        this.moduleComboBox.fillModules(project);
        this.robotComboBox = new ComboBox<>();
        CollectionComboBoxModel<RapidTask> taskComboBoxModel = new CollectionComboBoxModel<>();
        RapidRobot robot = RemoteRobotService.getInstance().getRobot();
        if (robot != null) {
            robotComboBox.addItem(robot);
            for (RapidTask task : robot.getTasks()) {
                taskComboBoxModel.add(task);
            }
        }

        moduleComboBox.allowEmptySelection(RapidBundle.message("run.configuration.panel.no.module"));

        this.taskComboBox = new ComboBox<>(taskComboBoxModel);
        if (taskComboBox.getItemCount() > 0) {
            taskComboBox.setSelectedIndex(0);
        }
        robotComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@NotNull JList<? extends RapidRobot> list, @Nullable RapidRobot value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    setText(value.getName());
                    setIcon(RapidIcons.ROBOT_ICON);
                }
            }
        });

        taskComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@NotNull JList<? extends RapidTask> list, @Nullable RapidTask value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    setText(value.getName());
                    setIcon(RapidIcons.TASK);
                }
            }
        });

        new ComponentValidator(this).withValidator(() -> {
            if (robotComboBox.getSelectedItem() == null) {
                return new ValidationInfo(RapidBundle.message("run.configuration.panel.validation.robot"), robotComboBox);
            }
            return null;
        }).andStartOnFocusLost().installOn(robotComboBox);

        new ComponentValidator(this).withValidator(() -> {
            System.out.println(taskComboBox.getSelectedItem());
            if (taskComboBox.getSelectedItem() == null) {
                return new ValidationInfo(RapidBundle.message("run.configuration.panel.validation.task"), taskComboBox);
            }
            return null;
        }).andStartOnFocusLost().installOn(taskComboBox);

        RobotEventListener.connect(new RobotEventListener() {
            @Override
            public void afterConnect(@NotNull RapidRobot robot) {
                robotComboBox.addItem(robot);
            }

            @Override
            public void beforeRemoval(@NotNull RapidRobot robot) {
                robotComboBox.removeItem(robot);
            }
        });

        RobotEventListener.connect(new RobotEventListener() {
            @Override
            public void afterConnect(@NotNull RapidRobot robot) {
                for (RapidTask task : robot.getTasks()) {
                    taskComboBoxModel.add(task);
                }
            }

            @Override
            public void afterRefresh(@NotNull RapidRobot robot) {
                taskComboBoxModel.removeAll();
                for (RapidTask task : robot.getTasks()) {
                    taskComboBoxModel.add(task);
                }
            }

            @Override
            public void beforeRemoval(@NotNull RapidRobot robot) {
                for (RapidTask task : robot.getTasks()) {
                    taskComboBoxModel.remove(task);
                }
            }
        });

        GridBag gridBag = new GridBag()
                .setDefaultAnchor(GridBagConstraints.WEST)
                .setDefaultWeightX(1.0)
                .setDefaultWeightY(1.0)
                .setDefaultInsets(10, UIUtil.DEFAULT_HGAP, 10, 10, 0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL);

        this.panel = new JPanel(new GridBagLayout());
        this.panel.add(LabeledComponent.create(moduleComboBox, RapidBundle.message("run.configuration.panel.module"), BorderLayout.WEST), gridBag.nextLine().next());
        this.panel.add(LabeledComponent.create(robotComboBox, RapidBundle.message("run.configuration.panel.robot"), BorderLayout.WEST), gridBag.nextLine().next());
        this.panel.add(LabeledComponent.create(taskComboBox, RapidBundle.message("run.configuration.panel.task"), BorderLayout.WEST), gridBag.nextLine().next());
    }

    public Project getProject() {
        return project;
    }

    @Override
    protected void resetEditorFrom(@NotNull RapidRunConfiguration s) {
        moduleComboBox.fillModules(getProject());
        moduleComboBox.setSelectedModule(s.getConfigurationModule().getModule());
        String robotPath = s.getOptions().getRobotPath();
        if (robotPath != null) {
            try {
                URI path = URI.create(robotPath);
                for (int i = 0; i < robotComboBox.getItemCount(); i++) {
                    RapidRobot robot = robotComboBox.getItemAt(i);
                    if (robot != null) {
                        if (robot.getPath().equals(path)) {
                            robotComboBox.setSelectedItem(robot);
                            break;
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
        String taskName = s.getOptions().getTaskName();
        CollectionComboBoxModel<RapidTask> model = ((CollectionComboBoxModel<RapidTask>) taskComboBox.getModel());
        for (RapidTask task : model.getItems()) {
            if (task.getName().equals(taskName)) {
                taskComboBox.setSelectedItem(task);
                break;
            }
        }
    }

    @Override
    protected void applyEditorTo(@NotNull RapidRunConfiguration s) throws ConfigurationException {
        s.setModule(((Module) moduleComboBox.getSelectedItem()));
        Object selectedTask = taskComboBox.getSelectedItem();
        if (selectedTask == null) {
            throw new ConfigurationException(RapidBundle.message("run.configuration.panel.validation.task"));
        }
        s.getOptions().setTaskName(((RapidTask) taskComboBox.getSelectedItem()).getName());
        Object selectedRobot = robotComboBox.getSelectedItem();
        if (selectedRobot == null) {
            throw new ConfigurationException(RapidBundle.message("run.configuration.panel.validation.robot"));
        }
        s.getOptions().setRobotPath(((RapidRobot) selectedRobot).getPath().toString());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

}
