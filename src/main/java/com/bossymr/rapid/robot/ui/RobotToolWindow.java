package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.EditSourceOnEnterKeyHandler;
import com.microsoft.z3.Symbol;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        TreeSpeedSearch.installOn(tree);

        AnAction action = ActionManager.getInstance().getAction("com.bossymr.rapid.robot.actions.ConnectRobotAction");

        tree.getEmptyText().setText(RapidBundle.message("robot.tool.window.no.content"));
        tree.getEmptyText().appendLine("");
        tree.getEmptyText().appendText(RapidBundle.message("robot.tool.window.no.content.action"),
                SimpleTextAttributes.LINK_PLAIN_ATTRIBUTES,
                e -> ActionUtil.invokeAction(action, panel, ActionPlaces.UNKNOWN, null, null));

        RobotEventListener.connect(new RobotEventListener() {

            @Override
            public void onConnect(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {
                model.invalidateAsync();
            }

            @Override
            public void onDisconnect(@NotNull RapidRobot robot) {
                model.invalidateAsync();
            }

            @Override
            public void onRefresh(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {
                model.invalidateAsync();
            }

            @Override
            public void onRemoval(@NotNull RapidRobot robot) {
                model.invalidateAsync();
            }

            @Override
            public void onSymbol(@NotNull RapidRobot robot, @NotNull VirtualSymbol symbol) {
                model.invalidateAsync();
            }

            @Override
            public void onDownload(@NotNull RapidRobot robot) {
                model.invalidateAsync();
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
            if (CommonDataKeys.PROJECT.is(dataId)) {
                return project;
            }
            if (PlatformCoreDataKeys.BGT_DATA_PROVIDER.is(dataId)) {
                return (DataProvider) this::getSlowData;
            }
            return null;
        }

        @SuppressWarnings("UnstableApiUsage")
        private @Nullable Object getSlowData(@NotNull @NonNls String dataId) {
            if (PlatformCoreDataKeys.SYMBOLS.is(dataId)) {
                return getSelectedSymbols();
            }
            if (PlatformCoreDataKeys.SELECTED_ITEM.is(dataId)) {
                AbstractTreeNode<?> selectedNode = getSelectedNode();
                return selectedNode != null ? selectedNode.getValue() : null;
            }
            if (PlatformCoreDataKeys.SELECTED_ITEMS.is(dataId)) {
                List<AbstractTreeNode<?>> selectedNodes = getSelectedNodes();
                if (selectedNodes == null) return null;
                return selectedNodes.stream()
                        .map(AbstractTreeNode::getValue)
                        .filter(Objects::nonNull)
                        .toArray(Object[]::new);
            }
            return null;
        }

        private @NotNull List<Symbol> getSelectedSymbols() {
            List<AbstractTreeNode<?>> selectedNodes = getSelectedNodes();
            if (selectedNodes == null) {
                return List.of();
            }
            return selectedNodes.stream()
                    .map(AbstractTreeNode::getValue)
                    .filter(element -> element instanceof Symbol)
                    .map(element -> ((Symbol) element))
                    .toList();
        }


        private @Nullable AbstractTreeNode<?> getSelectedNode() {
            TreePath treePath = tree.getSelectionPath();
            if (treePath == null) return null;
            return getSelectedNode(treePath);
        }

        private @Nullable List<AbstractTreeNode<?>> getSelectedNodes() {
            TreePath[] treePaths = tree.getSelectionPaths();
            if (treePaths == null) return null;
            List<AbstractTreeNode<?>> selectedNodes = new ArrayList<>();
            for (TreePath treePath : treePaths) {
                AbstractTreeNode<?> selectedNode = getSelectedNode(treePath);
                if (selectedNode == null) {
                    continue;
                }
                selectedNodes.add(selectedNode);
            }
            return selectedNodes;
        }

        private @Nullable AbstractTreeNode<?> getSelectedNode(@NotNull TreePath treePath) {
            Object component = treePath.getLastPathComponent();
            if (component instanceof DefaultMutableTreeNode mutableTreeNode) {
                Object userobject = mutableTreeNode.getUserObject();
                if (userobject instanceof NodeDescriptor<?> node) {
                    Object element = node.getElement();
                    if (element instanceof AbstractTreeNode<?> abstractTreeNode) {
                        return abstractTreeNode;
                    }
                }
            }
            return null;
        }
    }
}
