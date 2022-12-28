package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidOptionalArgumentImpl extends RapidCompositeElement implements RapidOptionalArgument {

    public RapidOptionalArgumentImpl() {
        super(RapidElementTypes.OPTIONAL_ARGUMENT);
    }

    @Override
    public @NotNull RapidReferenceExpression getParameter() {
        RapidExpression[] expressions = getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new);
        return (RapidReferenceExpression) expressions[0];
    }

    @Override
    public @Nullable RapidExpression getArgument() {
        RapidExpression[] expressions = getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new);
        return expressions.length > 1 ? expressions[1] : null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitOptionalArgument(this);
    }

    @Override
    public String toString() {
        return "RapidOptionalArgument:" + getParameter();
    }
}
