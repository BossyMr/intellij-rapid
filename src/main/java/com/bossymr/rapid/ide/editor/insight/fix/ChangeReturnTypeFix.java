package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.ASTNode;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ChangeReturnTypeFix extends PsiUpdateModCommandAction<PhysicalRoutine> {

    private final @NotNull RapidType newType;

    public ChangeReturnTypeFix(@NotNull PhysicalRoutine element, @NotNull RapidType newType) {
        super(element);
        this.newType = newType;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.change.return.type");
    }

    public String test() {
        return "";
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalRoutine element) {
        if (element.getRoutineType() != RoutineType.FUNCTION) {
            return null;
        }
        if(newType.isArray()) {
            return null;
        }
        return Presentation.of(RapidBundle.message("quick.fix.text.change.return.type", element.getPresentableName(), newType.getPresentableText()));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalRoutine element, @NotNull ModPsiUpdater updater) {
        RapidTypeElement typeElement = element.getTypeElement();
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(element.getProject());
        RapidTypeElement newTypeElement = elementFactory.createTypeElement(newType.getText());
        if(typeElement == null) {
            ASTNode head = element.getNode().findChildByType(element.getRoutineType().getElementType());
            Objects.requireNonNull(head);
            element.addAfter(newTypeElement, head.getPsi());
        } else {
            typeElement.replace(newTypeElement);
        }
    }
}
