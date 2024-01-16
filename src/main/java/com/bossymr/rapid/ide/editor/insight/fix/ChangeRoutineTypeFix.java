package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ChangeRoutineTypeFix extends PsiUpdateModCommandAction<PhysicalRoutine> {

    private final @NotNull RoutineType routineType;

    public ChangeRoutineTypeFix(@NotNull PhysicalRoutine element, @NotNull RoutineType routineType) {
        super(element);
        this.routineType = routineType;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.change.routine.type");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalRoutine element) {
        if (element.getRoutineType() == routineType) {
            return null;
        }
        return Presentation.of(RapidBundle.message("quick.fix.text.change.routine.type", element.getPresentableName(), routineType.getPresentableText()));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalRoutine element, @NotNull ModPsiUpdater updater) {
        RoutineType currentRoutineType = element.getRoutineType();
        ASTNode node = element.getNode();
        ASTNode head = node.findChildByType(currentRoutineType.getElementType());
        ASTNode tail = node.findChildByType(currentRoutineType.getTailType());
        Objects.requireNonNull(head);
        Objects.requireNonNull(tail);
        node.replaceChild(head, ASTFactory.leaf(routineType.getElementType(), routineType.getText()));
        node.replaceChild(tail, ASTFactory.leaf(routineType.getTailType(), "END" + routineType.getText()));
    }
}
