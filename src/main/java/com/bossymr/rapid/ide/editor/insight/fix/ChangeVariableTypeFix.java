package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameter;
import com.bossymr.rapid.language.symbol.physical.PhysicalTargetVariable;
import com.bossymr.rapid.language.symbol.physical.PhysicalVariable;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.ASTNode;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ChangeVariableTypeFix extends PsiUpdateModCommandAction<PhysicalVariable> {

    private final @NotNull RapidType newType;

    public ChangeVariableTypeFix(@NotNull PhysicalVariable element, @NotNull RapidType newType) {
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
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalVariable element) {
        if (element instanceof PhysicalTargetVariable) {
            return null;
        }
        return Presentation.of(RapidBundle.message("quick.fix.text.change.variable.type", element.getPresentableName(), newType.getPresentableText()));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalVariable element, @NotNull ModPsiUpdater updater) {
        RapidTypeElement typeElement = element.getTypeElement();
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(element.getProject());
        RapidTypeElement newTypeElement = elementFactory.createTypeElement(newType.getText());
        if (typeElement == null) {
            IElementType headType;
            if (element instanceof PhysicalField field) {
                headType = field.getFieldType().getElementType();
            } else if (element instanceof PhysicalParameter parameter) {
                headType = parameter.getParameterType().getElementType();
            } else {
                throw new IllegalArgumentException("Unexpected symbol: " + element);
            }
            ASTNode head = headType != null ? element.getNode().findChildByType(headType) : null;
            element.addAfter(newTypeElement, head != null ? head.getPsi() : null);
        } else {
            typeElement.replace(newTypeElement);
        }
        if (newType.isArray()) {
            if (element instanceof PhysicalField field) {
                RapidExpression expression = elementFactory.createExpressionFromText("<EXP>");
                List<RapidExpression> expressions = new ArrayList<>();
                for (int i = 0; i < newType.getDimensions(); i++) {
                    expressions.add(expression);
                }
                RapidArray array = elementFactory.createArray(expressions);
                RapidArray previousArray = field.getArray();
                if (previousArray != null) {
                    previousArray.replace(array);
                } else {
                    PsiElement anchor = Objects.requireNonNullElseGet(field.getNameIdentifier(), field::getTypeElement);
                    field.addAfter(array, anchor);
                }
            } else if (element instanceof PhysicalParameter parameter) {
                List<PsiElement> array = elementFactory.createArray(newType.getDimensions());
                ASTNode node = parameter.getNode();
                ASTNode arrayElement = node.findChildByType(RapidTokenTypes.LBRACE);
                while (arrayElement != null) {
                    ASTNode treeNext = arrayElement.getTreeNext();
                    if (arrayElement.getElementType() == RapidTokenTypes.RBRACE) {
                        treeNext = null;
                    }
                    arrayElement.getPsi().delete();
                    arrayElement = treeNext;
                }
                PsiElement anchor = Objects.requireNonNullElseGet(parameter.getNameIdentifier(), parameter::getTypeElement);
                parameter.addRangeAfter(array.get(0), array.get(array.size() - 1), anchor);
            }
        }
    }
}
