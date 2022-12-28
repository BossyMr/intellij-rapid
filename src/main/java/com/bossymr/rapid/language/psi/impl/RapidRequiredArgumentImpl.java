package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidRequiredArgumentImpl extends RapidCompositeElement implements RapidRequiredArgument {

    public RapidRequiredArgumentImpl() {
        super(RapidElementTypes.REQUIRED_ARGUMENT);
    }

    @Override
    public @Nullable RapidReferenceExpression getParameter() {
        RapidExpression[] expressions = getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new);
        return expressions.length == 2 ? (RapidReferenceExpression) expressions[0] : null;
    }

    @Override
    public @NotNull RapidExpression getArgument() {
        RapidExpression[] expressions = getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new);
        return expressions.length == 2 ? expressions[1] : expressions[0];
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
