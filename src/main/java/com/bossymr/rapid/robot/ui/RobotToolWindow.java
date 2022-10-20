package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotTopic;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.EditSourceOnEnterKeyHandler;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RobotToolWindow implements Disposable {

    public static final String ROBOT_TOOL_WINDOW_GROUP = "RobotToolWindow";
    private final Project project;

    private final RobotViewPanel content;
    private final SimpleToolWindowPanel panel;

    private final DnDAwareTree tree;

    public RobotToolWindow(@NotNull Project project) {
        this.project = project;

        RobotViewTreeStructure structure = new RobotViewTreeStructure(project);
        StructureTreeModel<RobotViewTreeStructure> model = new StructureTreeModel<>(structure, this);
        this.tree = new DnDAwareTree(new AsyncTreeModel(model, this));
        this.tree.setRootVisible(false);

        this.content = new RobotViewPanel();
        content.setLayout(new BorderLayout());

        this.panel = new SimpleToolWindowPanel(true, true);
        content.add(panel, BorderLayout.CENTER);

        panel.setToolbar(createActionsToolbar());

        JScrollPane treePane = ScrollPaneFactory.createScrollPane(tree);
        panel.setContent(treePane);

        AnAction contextMenu = ActionManager.getInstance().getAction("RobotContextMenu");

        PopupHandler.installPopupMenu(tree, (ActionGroup) contextMenu, "RobotToolWindow");

        EditSourceOnDoubleClickHandler.install(tree);
        EditSourceOnEnterKeyHandler.install(tree);

        project.getMessageBus().connect().subscribe(RobotTopic.ROBOT_TOPIC, new RobotTopic() {
            @Override
            public void onConnect(@NotNull Robot robot) {
                model.invalidate();
            }

            @Override
            public void onRefresh(@NotNull Robot robot) {
                model.invalidate();
            }

            @Override
            public void onDisconnect() {
                model.invalidate();
            }
        });
    }

    public @NotNull JComponent getComponent() {
        return content;
    }

    @Override
    public void dispose() {}

    public @NotNull JComponent createActionsToolbar() {
        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(ROBOT_TOOL_WINDOW_GROUP);
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("RobotToolbar", actionGroup, true);
        actionToolbar.setTargetComponent(content);
        return actionToolbar.getComponent();
    }

    private final class RobotViewPanel extends JPanel implements DataProvider {

        @Override
        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            if (CommonDataKeys.NAVIGATABLE_ARRAY.is(dataId)) {
                TreePath[] paths = tree.getSelectionPaths();
                if (paths == null) return null;
                List<Navigatable> objects = new ArrayList<>();
                for (TreePath path : paths) {
                    Object node = path.getLastPathComponent();
                    Object object = TreeUtil.getUserObject(node);
                    if (object instanceof Navigatable navigatable) {
                        objects.add(navigatable);
                    } else if (node instanceof Navigatable navigatable) {
                        objects.add(navigatable);
                    }
                }
                return objects.isEmpty() ? null : objects.toArray(Navigatable.EMPTY_NAVIGATABLE_ARRAY);
            }
            return null;
        }
    }
}
