package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidRequiredArgumentImpl extends RapidElementImpl implements RapidRequiredArgument {

    public RapidRequiredArgumentImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidReferenceExpression getParameter() {
        List<RapidExpression> elements = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return elements.size() == 2 ? (RapidReferenceExpression) elements.get(0) : null;
    }

    @Override
    public @NotNull RapidExpression getArgument() {
        List<RapidExpression> elements = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return elements.size() == 2 ? elements.get(1) : elements.get(0);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRequiredArgument(this);
    }

    @Override
    public String toString() {
        return "RapidRequiredArgument:" + getParameter();
    }
}
