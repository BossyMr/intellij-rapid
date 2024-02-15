package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidParameterList;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.ASTNode;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.tree.IElementType;
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
        ASTNode node = element.getNode();
        RoutineType currentType = element.getRoutineType();
        ASTNode head = node.findChildByType(currentType.getElementType());
        Objects.requireNonNull(head);
        ASTNode tail = node.findChildByType(currentType.getTailType());
        Objects.requireNonNull(tail);
        PsiManager manager = element.getManager();
        node.replaceChild(head, createLeaf(routineType.getElementType(), routineType.getText(), manager));
        node.replaceChild(tail, createLeaf(routineType.getTailType(), "END" + routineType.getText(), manager));
        RapidParameterList parameterList = element.getParameterList();
        if (routineType == RoutineType.TRAP) {
            if (parameterList != null) {
                parameterList.delete();
            }
        }
        if (routineType != RoutineType.TRAP) {
            if (parameterList == null) {
                PsiElement identifier = element.getNameIdentifier();
                if (identifier != null) {
                    RapidParameterList result = RapidElementFactory.getInstance(context.project()).createParameterList();
                    element.addAfter(identifier, result);
                }
            }
        }
    }

    private @NotNull ASTNode createLeaf(@NotNull IElementType elementType, @NotNull String text, @NotNull PsiManager manager) {
        return Factory.createSingleLeafElement(elementType, text, null, manager);
    }
}
