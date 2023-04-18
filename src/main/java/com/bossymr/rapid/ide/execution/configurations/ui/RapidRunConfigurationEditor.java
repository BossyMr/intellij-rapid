package com.bossymr.rapid.ide.execution.configurations.ui;

import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.RobotEventListener;
import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.TableUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RapidRunConfigurationEditor extends SettingsEditor<RapidRunConfiguration> {

    private final @NotNull ComboBox<RapidRobot> robotComboBox;

    private final @NotNull TableView<TaskState> taskTable;
    private final @NotNull ListTableModel<TaskState> taskModel;

    private final @NotNull JPanel panel;

    private final @NotNull Project project;

    public RapidRunConfigurationEditor(@NotNull Project project) {
        this.project = project;
        this.robotComboBox = new ComboBox<>();
        EnabledColumnInfo ENABLED = new EnabledColumnInfo();
        NameColumnInfo NAME = new NameColumnInfo();
        ModuleColumnInfo MODULE = new ModuleColumnInfo(project);
        this.taskModel = new ListTableModel<>(new ColumnInfo[]{ENABLED, NAME, MODULE});
        RapidRobot robot = RemoteRobotService.getInstance().getRobot();
        if (robot != null) {
            robotComboBox.addItem(robot);
            taskModel.setItems(robot.getTasks().stream()
                    .map(task -> new TaskState(task.getName(), true, null))
                    .toList());
        }
        this.taskTable = new TableView<>(taskModel);
        taskTable.getEmptyText().setText(RapidBundle.message("run.configuration.panel.no.task"));

        taskTable.setColumnSelectionAllowed(false);
        taskTable.setShowGrid(false);
        taskTable.setDragEnabled(false);
        taskTable.setShowHorizontalLines(false);
        taskTable.setShowVerticalLines(false);
        taskTable.setIntercellSpacing(new Dimension(0, 0));

        TableUtil.setupCheckboxColumn(taskTable, 0);
        robotComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@NotNull JList<? extends RapidRobot> list, @Nullable RapidRobot value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    setText(value.getName());
                    setIcon(RapidIcons.ROBOT_ICON);
                }
            }
        });

        new ComponentValidator(this).withValidator(() -> {
            if (robotComboBox.getSelectedItem() == null) {
                return new ValidationInfo(RapidBundle.message("run.configuration.panel.validation.robot"), robotComboBox);
            }
            return null;
        }).andStartOnFocusLost().installOn(robotComboBox);

        robotComboBox.addActionListener(e -> {
            Object selectedItem = robotComboBox.getSelectedItem();
            if (selectedItem instanceof RapidRobot selected) {
                taskModel.setItems(selected.getTasks().stream()
                        .map(task -> {
                            TaskState taskState = new TaskState();
                            taskState.setName(task.getName());
                            return taskState;
                        })
                        .toList());
            }
            if (selectedItem == null) {
                taskModel.setItems(new ArrayList<>());
            }
        });

        RobotEventListener.connect(new RobotEventListener() {
            @Override
            public void onConnect(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {
                robotComboBox.addItem(robot);
            }

            @Override
            public void onRemoval(@NotNull RapidRobot robot) {
                robotComboBox.removeItem(robot);
            }
        });

        GridBag gridBag = new GridBag()
                .setDefaultAnchor(GridBagConstraints.WEST)
                .setDefaultWeightX(1.0)
                .setDefaultWeightY(1.0)
                .setDefaultInsets(10, UIUtil.DEFAULT_HGAP, 10, 10, 0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL);

        this.panel = new JPanel(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.add(LabeledComponent.create(robotComboBox, RapidBundle.message("run.configuration.panel.robot"), BorderLayout.WEST), gridBag.nextLine().next());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(ScrollPaneFactory.createScrollPane(taskTable), BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.NORTH);
        panel.add(new TitledSeparator(RapidBundle.message("run.configuration.panel.task")), BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
    }

    public @NotNull Project getProject() {
        return project;
    }

    @Override
    protected void resetEditorFrom(@NotNull RapidRunConfiguration s) {
        String robotPath = s.getOptions().getRobotPath();
        if (robotPath != null) {
            try {
                URI path = URI.create(robotPath);
                for (int i = 0; i < robotComboBox.getItemCount(); i++) {
                    RapidRobot robot = robotComboBox.getItemAt(i);
                    if (robot != null) {
                        if (robot.getPath().equals(path)) {
                            robotComboBox.setSelectedItem(robot);
                            Map<RapidTask, TaskState> tasks = s.getOptions().getRobotTasks().stream()
                                    .filter(task -> task.getName() != null)
                                    .collect(Collectors.toMap(task -> robot.getTask(task.getName()), task -> new TaskState(task.getName(), task.isEnabled(), task.getModuleName())));
                            List<TaskState> updated = new ArrayList<>();
                            for (RapidTask task : robot.getTasks()) {
                                if (tasks.containsKey(task)) {
                                    updated.add(tasks.get(task));
                                } else {
                                    TaskState taskState = new TaskState();
                                    taskState.setName(taskState.getName());
                                    updated.add(taskState);
                                }
                            }
                            taskModel.setItems(updated);
                            break;
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @Override
    protected void applyEditorTo(@NotNull RapidRunConfiguration s) throws ConfigurationException {
        Object selectedRobot = robotComboBox.getSelectedItem();
        if (selectedRobot == null) {
            throw new ConfigurationException(RapidBundle.message("run.configuration.panel.validation.robot"));
        }
        List<TaskState> tasks = taskTable.getItems();
        if (tasks.stream().noneMatch(TaskState::isEnabled)) {
            throw new ConfigurationException(RapidBundle.message("run.configuration.panel.validation.task"));
        }
        String robotPath = ((RapidRobot) selectedRobot).getPath().toString();
        s.getOptions().setRobotPath(robotPath);
        s.getOptions().setRobotTasks(tasks);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return panel;
    }

    public static class EnabledColumnInfo extends ColumnInfo<TaskState, Boolean> {

        public EnabledColumnInfo() {
            super("");
        }

        @Override
        public Class<?> getColumnClass() {
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(TaskState taskState) {
            return true;
        }

        @Override
        public @Nullable Boolean valueOf(@NotNull TaskState taskState) {
            return taskState.isEnabled();
        }

        @Override
        public void setValue(@NotNull TaskState taskState, @NotNull Boolean value) {
            taskState.setEnabled(value);
        }
    }

    public static class NameColumnInfo extends ColumnInfo<TaskState, String> {

        public NameColumnInfo() {
            super(RapidBundle.message("run.configuration.panel.task.name"));
        }

        @Override
        public @Nullable String valueOf(@NotNull TaskState taskState) {
            return taskState.getName();
        }
    }

    public static class ModuleColumnInfo extends ColumnInfo<TaskState, Module> {

        private final @NotNull Project project;

        public ModuleColumnInfo(@NotNull Project project) {
            super(RapidBundle.message("run.configuration.panel.task.module"));
            this.project = project;
        }

        @Override
        public @Nullable TableCellRenderer getRenderer(@NotNull TaskState taskState) {
            return new DefaultTableCellRenderer() {
                @Override
                public @NotNull Component getTableCellRendererComponent(@NotNull JTable table, @Nullable Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component delegate = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (value instanceof Module module) {
                        setIcon(ModuleType.get(module).getIcon());
                        setText(module.getName());
                    }
                    return delegate;
                }
            };
        }

        @Override
        public Class<?> getColumnClass() {
            return Module.class;
        }

        @Override
        public @Nullable Module valueOf(@NotNull TaskState taskState) {
            if (taskState.getModuleName() == null) return null;
            return ModuleManager.getInstance(project).findModuleByName(taskState.getModuleName());
        }

        @Override
        public void setValue(@NotNull TaskState taskState, @Nullable Module value) {
            String moduleName = value != null ? value.getName() : null;
            taskState.setModuleName(moduleName);
        }

        @Override
        public boolean isCellEditable(TaskState taskState) {
            return true;
        }

        @Override
        public @Nullable TableCellEditor getEditor(@NotNull TaskState taskState) {
            ModulesComboBox moduleComboBox = new ModulesComboBox();
            moduleComboBox.fillModules(project);
            moduleComboBox.allowEmptySelection("");
            DefaultCellEditor defaultCellEditor = new DefaultCellEditor(moduleComboBox);
            defaultCellEditor.setClickCountToStart(1);
            return defaultCellEditor;
        }
    }

}
