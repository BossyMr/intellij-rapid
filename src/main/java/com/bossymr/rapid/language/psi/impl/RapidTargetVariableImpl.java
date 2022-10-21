package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidTargetVariableImpl extends RapidCompositeElement implements RapidTargetVariable {

    public RapidTargetVariableImpl() {
        super(RapidElementTypes.TARGET_VARIABLE);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findPsiChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
    }

    @Override
    public String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public @Nullable RapidType getType() {
        return RobotService.DataType.NUMBER.getType(getProject());
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTargetVariable(this);
    }

    @Override
    public String toString() {
        return "RapidTargetVariable:" + getName();
    }
}
