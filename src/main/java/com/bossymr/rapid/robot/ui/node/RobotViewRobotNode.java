package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RobotViewRobotNode extends RobotViewNode<RapidRobot> {

    public RobotViewRobotNode(@NotNull Project project, @NotNull RapidRobot node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        if (getValue() == null) {
            return Collections.emptyList();
        }
        Set<String> symbols = Set.of("atm", "rec", "ali", "var", "prc");
        List<RobotViewNode<?>> nodes = new ArrayList<>();
        for (RapidTask task : getValue().getTasks()) {
            nodes.add(new RobotViewTaskNode(getProject(), task));
        }
        RobotViewDirectoryNode<String> directoryNode = new RobotViewDirectoryNode<>(getProject(), "Symbols", RapidIcons.ROBOT_DIRECTORY, symbols) {
            @Override
            public @NotNull AbstractTreeNode<?> getChild(@NotNull String value) {
                RapidRobot robot = RemoteRobotService.getInstance().getRobot();
                Set<RapidSymbol> symbols = robot != null ? robot.getSymbols().stream()
                        .filter(symbol -> switch (value) {
                            case "atm" -> symbol instanceof RapidAtomic;
                            case "rec" -> symbol instanceof RapidRecord;
                            case "ali" -> symbol instanceof RapidAlias;
                            case "con", "var", "per" -> symbol instanceof RapidField;
                            case "fun", "prc", "trp" -> symbol instanceof RapidRoutine;
                            default -> throw new IllegalStateException();
                        }).collect(Collectors.toSet()) : new HashSet<>();

                String title = switch (value) {
                    case "atm" -> RapidBundle.message("robot.tool.window.symbol.atomic");
                    case "rec" -> RapidBundle.message("robot.tool.window.symbol.record");
                    case "ali" -> RapidBundle.message("robot.tool.window.symbol.alias");
                    case "con", "var", "per" -> RapidBundle.message("robot.tool.window.symbol.field");
                    case "fun", "prc", "trp" -> RapidBundle.message("robot.tool.window.symbol.routine");
                    default -> throw new IllegalStateException();
                };

                return new RobotViewDirectoryNode<>(getProject(), title, RapidIcons.ROBOT_DIRECTORY, symbols) {
                    @Override
                    public @NotNull AbstractTreeNode<?> getChild(@NotNull RapidSymbol value) {
                        return new RobotViewSymbolNode(getProject(), value);
                    }
                };
            }

        };
        nodes.add(directoryNode);
        return nodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        RapidRobot value = getValue();
        if (value != null) {
            presentation.setIcon(RapidIcons.ROBOT_ICON);
            presentation.setPresentableText(value.getName());
        }
    }
}
