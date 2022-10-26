package com.bossymr.rapid.robot.ui.node;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.controller.rapid.SymbolType;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class RobotViewRobotNode extends RobotViewNode<Robot> {

    public RobotViewRobotNode(@NotNull Project project, @NotNull Robot node) {
        super(project, node);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        SymbolType[] symbols = new SymbolType[]{SymbolType.ATOMIC, SymbolType.RECORD, SymbolType.ALIAS, SymbolType.VARIABLE, SymbolType.PROCEDURE};
        RobotViewDirectoryNode<SymbolType> directoryNode = new RobotViewDirectoryNode<>(getProject(), "Symbols", RapidIcons.ROBOT_DIRECTORY, Set.of(symbols)) {
            @Override
            public @NotNull AbstractTreeNode<?> getChild(@NotNull SymbolType value) {
                Set<RapidSymbol> symbols = RobotService.getInstance(getProject()).getSymbols().stream()
                        .filter(symbol -> switch (value) {
                            case ATOMIC -> symbol instanceof RapidAtomic;
                            case RECORD -> symbol instanceof RapidRecord;
                            case ALIAS -> symbol instanceof RapidAlias;
                            case CONSTANT, VARIABLE, PERSISTENT -> symbol instanceof RapidField;
                            case FUNCTION, PROCEDURE, TRAP -> symbol instanceof RapidRoutine;
                            default -> throw new IllegalStateException();
                        }).collect(Collectors.toSet());

                String title = switch (value) {
                    case ATOMIC -> RapidBundle.message("robot.tool.window.symbol.atomic");
                    case RECORD -> RapidBundle.message("robot.tool.window.symbol.record");
                    case ALIAS -> RapidBundle.message("robot.tool.window.symbol.alias");
                    case CONSTANT, VARIABLE, PERSISTENT -> RapidBundle.message("robot.tool.window.symbol.field");
                    case FUNCTION, PROCEDURE, TRAP -> RapidBundle.message("robot.tool.window.symbol.routine");
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
        return Collections.singletonList(directoryNode);
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setIcon(RapidIcons.ROBOT_ICON);
        presentation.setPresentableText(getValue().getName());
    }
}
