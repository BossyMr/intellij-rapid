package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidConditionalArgumentImpl extends PhysicalElement implements RapidConditionalArgument {

    public RapidConditionalArgumentImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull RapidReferenceExpression getParameter() {
        List<PsiElement> expressions = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return (RapidReferenceExpression) expressions.get(0);
    }

    @Override
    public @Nullable RapidExpression getArgument() {
        List<PsiElement> expressions = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return expressions.size() >= 2 ? (RapidExpression) expressions.get(1) : null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitConditionalArgument(this);
    }

    @Override
    public String toString() {
        return "RapidConditionalArgument:" + getParameter();
    }
}
