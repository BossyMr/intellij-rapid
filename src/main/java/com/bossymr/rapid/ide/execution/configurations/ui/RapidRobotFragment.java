package com.bossymr.rapid.ide.execution.configurations.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.client.HeavyNetworkManager;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.ui.RobotConnectPanel;
import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.SettingsEditorFragment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RapidRobotFragment extends SettingsEditorFragment<RapidRunConfiguration, JComponent> {

    private final @NotNull RobotConnectPanel panel = new RobotConnectPanel();
    private final @NotNull TableView<TaskState> table;
    private final @NotNull ListTableModel<TaskState> model;

    public RapidRobotFragment(@NotNull Project project) {
        super("robot", RapidBundle.message("run.configuration.fragment.robot"), null, null, null, null, configuration -> true);

        ColumnInfo<TaskState, String> TASK_NAME = new NameColumnInfo();
        ColumnInfo<TaskState, Boolean> IS_ENABLED = new EnableColumnInfo();
        ColumnInfo<TaskState, Module> UPLOAD_MODULE = new UploadColumnInfo(project);

        model = new ListTableModel<>(TASK_NAME, IS_ENABLED, UPLOAD_MODULE);
        table = new TableView<>(model);
        table.getEmptyText().setText(RapidBundle.message("run.configuration.fragment.task.no.selection"));

        JTableHeader header = table.getTableHeader();
        FontMetrics metrics = header.getFontMetrics(header.getFont());

        TableColumn column = table.getColumnModel().getColumn(1);
        column.setCellRenderer(new BooleanTableCellRenderer());
        setColumnWidth(column, metrics.stringWidth(IS_ENABLED.getName()) + 20);

        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setShowGrid(false);
        table.setDragEnabled(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setupEasyFocusTraversing();

        myComponent = new JPanel(new BorderLayout());
        myComponent.add(panel.getPanel(), BorderLayout.NORTH);
        myComponent.add(new TitledSeparator(RapidBundle.message("run.configuration.fragment.task")), BorderLayout.CENTER);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(table);
        setMinimumHeight(scrollPane);
        myComponent.add(scrollPane, BorderLayout.SOUTH);

        setValidation(configuration -> {
            List<ValidationInfo> results = new ArrayList<>(panel.validate());
            if (!(results.isEmpty())) {
                return results;
            }
            if (model.getItems().isEmpty()) {
                results.add(new ValidationInfo(RapidBundle.message("run.configuration.fragment.task.validation.empty"), scrollPane));
            } else if (model.getItems().stream().noneMatch(TaskState::isEnabled)) {
                results.add(new ValidationInfo(RapidBundle.message("run.configuration.fragment.task.validation.not.enabled"), scrollPane));
            }
            return results;
        });

        panel.onModified(model -> {
            Credentials credentials = model.getCredentials();
            assert credentials != null;
            refreshEditorFrom(model.getHost(), credentials);
            return null;
        });
    }

    @Override
    protected void disposeEditor() {
        panel.close();
    }

    @Override
    protected void resetEditorFrom(@NotNull RapidRunConfiguration configuration) {
        String address = configuration.getOptions().getPath();
        URI host = address != null ? URI.create(address) : null;
        String username = configuration.getOptions().getUsername();
        if(username == null) {
            panel.reset(new RobotConnectPanel.Model(host, RobotService.DEFAULT_CREDENTIALS));
            return;
        }
        model.setItems(createCopy(configuration.getOptions().getTasks()));
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String password = configuration.getOptions().getPassword();
            Credentials credentials = password != null ? new Credentials(username, password) : null;
            ApplicationManager.getApplication().invokeLater(() -> panel.reset(new RobotConnectPanel.Model(host, credentials)), ModalityState.any());
        });
    }

    @Override
    protected void applyEditorTo(@NotNull RapidRunConfiguration configuration) {
        /*
         * Apply this panel to the specified run configuration, which must not be a snapshot.
         */
        applyToSnapshot(configuration);
        RobotConnectPanel.Model model = panel.apply();
        URI host = model.getHost();
        Credentials credentials = model.getCredentials();
        if(host != null && credentials != null) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> configuration.getOptions().setPassword(new String(credentials.password())));
        }
    }

    private void applyToSnapshot(@NotNull RapidRunConfiguration configuration) {
        /*
         * Apply this panel to the specified run configuration, which might be a snapshot.
         */
        RobotConnectPanel.Model model = panel.applyToSnapshot();
        URI host = model.getHost();
        Credentials credentials = model.getCredentials();
        if (host == null || credentials == null) {
            return;
        }
        // The username 'null' means that the authentication type is set to 'default credentials'.
        String username = credentials == RobotService.DEFAULT_CREDENTIALS ? null : credentials.username();
        configuration.getOptions().setUsername(username);
        configuration.getOptions().setPath(host.toString());
        // A copy needs to be made of all states, otherwise, if a task is modified the task state is automatically
        // modified, even if you didn't press apply.
        List<TaskState> taskStates = createCopy(this.model.getItems());
        configuration.getOptions().setTasks(taskStates);
    }

    private @NotNull List<TaskState> createCopy(@NotNull List<TaskState> states) {
        return states.stream()
                     .map(state -> new TaskState(state.getName(), state.isEnabled(), state.getModuleName()))
                .toList();
    }

    @Override
    public @NotNull RapidRunConfiguration getSnapshot() throws ConfigurationException {
        /*
         * By default, when this fragment checks if it is modified, it creates a new run configuration and applies the
         * fragment to it. Thereafter, it checks if the new run configuration is different from the current run
         * configuration.
         *
         * However, since this fragment attempts to store the current password to the password safe, it will attempt
         * to store the password every time the UI checks if the panel is modified. As a result, we avoid storing
         * the password when computing a snapshot. To check if the password is modified, a listener is created (see
         * RobotConnectPanel#addPasswordListener(Runnable)).
         */
        if (getOwner() != null) {
            return getOwner().getSnapshot();
        }
        RapidRunConfiguration settings = getFactory().create();
        applyToSnapshot(settings);
        return settings;
    }

    @Override
    public boolean isSpecificallyModified() {
        return panel.isModified();
    }

    private void setColumnWidth(@NotNull TableColumn column, int preferredWidth) {
        column.setWidth(preferredWidth);
        column.setPreferredWidth(preferredWidth);
        column.setMinWidth(preferredWidth);
        column.setMaxWidth(preferredWidth);
    }

    private void setMinimumHeight(@NotNull JComponent component) {
        int preferredWidth = (int) component.getPreferredSize().getWidth();
        component.setPreferredSize(new Dimension(preferredWidth, 100));
        int minimumWidth = (int) component.getMinimumSize().getWidth();
        component.setMinimumSize(new Dimension(minimumWidth, 100));
    }

    public void refreshEditorFrom(@Nullable URI path, @NotNull Credentials credentials) {
        if (path == null) {
            table.getEmptyText().setText(RapidBundle.message("run.configuration.fragment.task.no.selection"));
            model.setItems(List.of());
            return;
        }
        String presentablePath = path.getHost() + (path.getPort() != 80 ? ":" + path.getPort() : "");
        table.getEmptyText().setText(RapidBundle.message("run.configuration.fragment.task.connecting", presentablePath));
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try (NetworkManager manager = new HeavyNetworkManager(path, credentials)) {
                TaskService taskService = manager.createService(TaskService.class);
                List<Task> tasks = taskService.getTasks().get();
                Map<String, TaskState> taskStates = new HashMap<>();
                for (TaskState taskState : model.getItems()) {
                    if (tasks.stream().noneMatch(task -> task.getName().equalsIgnoreCase(taskState.getName()))) {
                        // This task does not exist on the new robot.
                        continue;
                    }
                    taskStates.put(taskState.getName().toLowerCase(), taskState);
                }
                for (Task task : tasks) {
                    if (taskStates.containsKey(task.getName().toLowerCase())) {
                        // This task also existed on the previous robot.
                        continue;
                    }
                    taskStates.put(task.getName().toLowerCase(), new TaskState(task.getName(), true, null));
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    table.getEmptyText().setText(RapidBundle.message("run.configuration.fragment.task.empty"));
                    model.setItems(List.copyOf(taskStates.values()));
                }, ModalityState.any());
            } catch (IOException | InterruptedException e) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    table.getEmptyText().setText(RapidBundle.message("run.configuration.fragment.task.invalid", presentablePath), SimpleTextAttributes.ERROR_ATTRIBUTES);
                    model.setItems(List.of());
                }, ModalityState.any() );
            }
        });
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

    public static class EnableColumnInfo extends ColumnInfo<TaskState, Boolean> {

        public EnableColumnInfo() {
            super(RapidBundle.message("run.configuration.panel.task.enable"));
        }

        @Override
        public Class<?> getColumnClass() {
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(@NotNull TaskState taskState) {
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

    public static class UploadColumnInfo extends ColumnInfo<TaskState, Module> {

        private final Project project;

        public UploadColumnInfo(@NotNull Project project) {
            super(RapidBundle.message("run.configuration.panel.task.module"));
            this.project = project;
        }

        @Override
        public Class<?> getColumnClass() {
            return Module.class;
        }

        @Override
        public @Nullable Module valueOf(@NotNull TaskState taskState) {
            String moduleName = taskState.getModuleName();
            if (moduleName == null) {
                return null;
            }
            ModuleManager manager = ModuleManager.getInstance(project);
            return manager.findModuleByName(moduleName);
        }

        @Override
        public void setValue(@NotNull TaskState taskState, @Nullable Module value) {
            taskState.setModuleName(value != null ? value.getName() : null);
        }

        @Override
        public boolean isCellEditable(TaskState taskState) {
            return true;
        }

        @Override
        public @Nullable TableCellEditor getEditor(TaskState taskState) {
            ModulesComboBox comboBox = new ModulesComboBox();
            comboBox.fillModules(project);
            comboBox.allowEmptySelection("");
            DefaultCellEditor editor = new DefaultCellEditor(comboBox);
            editor.setClickCountToStart(1);
            return editor;
        }

        @Override
        public @Nullable TableCellRenderer getRenderer(TaskState taskState) {
            return new DefaultTableCellRenderer() {
                @Override
                public @NotNull Component getTableCellRendererComponent(@NotNull JTable table, @Nullable Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (value instanceof Module module) {
                        setIcon(ModuleType.get(module).getIcon());
                        setName(module.getName());
                    }
                    return component;
                }
            };
        }
    }
}
