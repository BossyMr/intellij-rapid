package com.bossymr.rapid.ide.execution;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RapidRunLineMarkerContributor extends RunLineMarkerContributor {

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        IElementType elementType = element.getNode().getElementType();
        if (!(elementType == RapidTokenTypes.IDENTIFIER)) {
            return null;
        }
        PsiElement parent = element.getParent();
        if (!(parent instanceof RapidRoutine routine)) {
            return null;
        }
        String name = routine.getName();
        if (name == null) {
            return null;
        }
        if (routine.getRoutineType() != RoutineType.PROCEDURE) {
            return null;
        }
        if (!(name.equalsIgnoreCase("main"))) {
            return null;
        }
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters == null) {
            return null;
        }
        if (!(parameters.isEmpty())) {
            return null;
        }
        RapidRobot robot = RobotService.getInstance().getRobot();
        if(robot == null || robot.getTasks().isEmpty()) {
            return null;
        }
        AnAction[] actions = ExecutorAction.getActions();
        return new Info(AllIcons.RunConfigurations.TestState.Run, actions, caller -> {
            AnActionEvent event = createActionEvent(caller);
            return Arrays.stream(actions)
                    .map(action -> getText(action, event))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
        });
    }
}
